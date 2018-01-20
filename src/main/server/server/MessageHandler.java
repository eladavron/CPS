package server;

import Exceptions.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import controller.CustomerController;
import entity.*;
import ocsf.server.ConnectionToClient;
import utils.TimeUtils;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;

import static controller.Controllers.*;
import static controller.CustomerController.SubscriptionOperationReturnCodes;
import static controller.CustomerController.SubscriptionOperationReturnCodes.QUERY_RESPONSE;
import static entity.Message.DataType.*;
import static entity.Message.MessageType;
import static entity.Message.MessageType.*;
import static entity.Order.OrderStatus.IN_PROGRESS;
import static entity.Report.ReportType;

/**
 * Handles messages from client GUI:
 *  1. Query
 *  2. Create
 *  3. Update
 *  4. Delete
 */
public class MessageHandler {
    private static ObjectMapper mapper = new ObjectMapper();

    public static void sendToClient(Message message, ConnectionToClient clientConnection) throws IOException {
        String json = message.toJson();
        if (CPSServer.IS_DEBUG)
        {
            System.out.println("SENT (" + clientConnection.getInetAddress() + "): " + json);
        }
        clientConnection.sendToClient(json);
    }

    public static boolean handleMessage(String json, ConnectionToClient clientConnection) throws IOException {
        try {
            Message msg = new Message(json);
            MessageType msgType = msg.getMessageType();

            if (SessionManager.doesSessionExists(clientConnection))
            {
                SessionManager.getSession(clientConnection).getTransMap().putIfAbsent(msg.getTransID(), msg.getDataType());
            }
            if (msgType == MessageType.LOGOUT)
            {
                handleLogout(msg,clientConnection);
                return true;
            }

            Message replyOnReceiveMsg = new Message(MessageType.QUEUED, PRIMITIVE, "Yes sir! will do!");
            replyOnReceiveMsg.setTransID(msg.getTransID());
            sendToClient(replyOnReceiveMsg, clientConnection);

            switch(msgType)
            {
                case LOGIN:
                    handleLogin(msg, clientConnection);
                    break;
                case QUERY:
                    handleQueries(msg, clientConnection);
                    break;
                case CREATE:
                    handleCreation(msg, clientConnection);
                    break;
                case PAYMENT:
                    handlePayment(msg, clientConnection);
                    break;
                case END_PARKING:
                    handleEndParking(msg, clientConnection);
                    break;
                case DELETE:
                    handleDeletion(msg, clientConnection);
                    break;
                case UPDATE:
                    handleUpdate(msg, clientConnection);

                    break;
                default:
                    throw new InvalidMessageException("Unknown message type: " + msgType);
            }
        } catch (EmployeeNotificationFailureException e) {
            Message employeeNotification = new Message(MessageType.ERROR_OCCURRED, PRIMITIVE,"\nAN ERROR HAS OCCURRED )-:\n" + e.getMessage());
            Long SID = Message.getSidFromJson(json);
            if (SID != null)
                employeeNotification.setTransID(SID);
            sendToClient(employeeNotification, clientConnection);
        } catch (CustomerNotificationFailureException e) {
            Message customerNotification = new Message(MessageType.ERROR_OCCURRED, PRIMITIVE,"\n\nDear valued customer:\n" + e.getMessage());
            Long SID = Message.getSidFromJson(json);
            if (SID != null)
                customerNotification.setTransID(SID);
            sendToClient(customerNotification, clientConnection);
            return false;
        } catch (Exception e) {
            Message replyInvalid = new Message(MessageType.ERROR_OCCURRED, PRIMITIVE, "\nException: " + e.getMessage());
            Long SID = Message.getSidFromJson(json);
            if (SID != null)
                replyInvalid.setTransID(SID);
            sendToClient(replyInvalid, clientConnection);
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private static void handleEndParking(Message endParkingMsg, ConnectionToClient clientConnection) throws Exception {

        Message endParkingResponse = new Message();
        endParkingResponse.setTransID(endParkingMsg.getTransID());
        Integer orderId = ((Order)endParkingMsg.getData().get(0)).getOrderID();
        try{
            Order orderToFinish = orderController.getOrder(orderId);
            orderId = orderToFinish.getOrderID();

            Customer departingCustomer = customerController.getCustomer(orderToFinish.getCostumerID());
            orderToFinish.setActualExitTime(new Date());
            Double remainingCharge = orderController.handleFinalBillingUponDeparture(departingCustomer,orderToFinish);
            if (remainingCharge > 0.0)
            {
                SessionManager.getSession(clientConnection).setOrderInNeedOfPayment(orderToFinish);
                endParkingResponse.setMessageType(NEED_PAYMENT);
                endParkingResponse.setDataType(PRIMITIVE);
                endParkingResponse.addData(remainingCharge);
            }
            else
            {
                customerController.finishOrder(departingCustomer,orderToFinish.getOrderID(),orderToFinish.getPrice());
                parkingController.exitParkingLot(orderToFinish.getOrderID());
                orderToFinish.setOrderStatus(Order.OrderStatus.FINISHED);
                endParkingResponse.setMessageType(FINISHED);
                endParkingResponse.setDataType(PRIMITIVE);
                if (remainingCharge < 0)
                {
                    endParkingResponse.addData(Math.abs(remainingCharge));
                }
            }

            sendToClient(endParkingResponse, clientConnection);

        }catch (OrderNotFoundException e){
            endParkingResponse.setMessageType(MessageType.FAILED);
            endParkingResponse.setDataType(PRIMITIVE);
            endParkingResponse.addData("Order ID " + orderId + " was not found, please contact CPS personnel");
            sendToClient(endParkingResponse, clientConnection);
        }
    }

    private static void handlePayment(Message paymentNeededMsg, ConnectionToClient clientConnection) throws Exception {
        Message response            = new Message();
        Message.DataType orderType  = SessionManager.getSession(clientConnection).getTransMap().get(paymentNeededMsg.getTransID());
        switch(orderType) {
            case PREORDER:
                PreOrder preorderInNeedOfPayment = (PreOrder) SessionManager.getSession(clientConnection).getOrderInNeedOfPayment();
                SessionManager.getSession(clientConnection).setOrderInNeedOfPayment(null);

                Order newPreOrder = customerController.addNewPreOrder(preorderInNeedOfPayment);

                response.setMessageType(FINISHED);
                response.setDataType(orderType);
                response.addData(newPreOrder);
                break;
            case ORDER:
                Order    orderInNeedOfPayment  = SessionManager.getSession(clientConnection).getOrderInNeedOfPayment();
                Customer payingCustomer        = SessionManager.getSession(clientConnection).getCustomer();

                if (orderInNeedOfPayment != null) {
                    parkingController.exitParkingLot(orderInNeedOfPayment.getOrderID());
                    customerController.finishOrder(payingCustomer, orderInNeedOfPayment.getOrderID(),orderInNeedOfPayment.getPrice());
                    SessionManager.getSession(clientConnection).setOrderInNeedOfPayment(null);
                    response.setMessageType(FINISHED);
                    response.setDataType(orderType);
                    response.addData(orderInNeedOfPayment);
                } else {
                    response.setMessageType(MessageType.FAILED);
                    response.setDataType(PRIMITIVE);
                    response.addData("Are we feeling generous today?");
                }
                break;
            case SUBSCRIPTION:
                Subscription subscription = SessionManager.getSession(clientConnection).getSubscriptionInNeedOfPayment();

                CustomerController.SubscriptionOperationReturnCodes rc = SubscriptionOperationReturnCodes.FAILED;
                if (subscription instanceof  RegularSubscription)
                {
                    rc = customerController.addNewRegularSubscription((RegularSubscription) subscription);
                }
                else if (subscription instanceof FullSubscription)
                {
                    rc = customerController.addNewFullSubscription((FullSubscription) subscription);
                }
                else
                {
                    throw new NotImplementedException("Subscription type does not exists: " + subscription.getSubscriptionType().toString());
                }

                if (rc != CustomerController.SubscriptionOperationReturnCodes.FAILED)
                {
                    response = new Message(FINISHED, SUBSCRIPTION, rc, subscription);
                    SessionManager.getSession(clientConnection).setSubscriptionInNeedOfPayment(null);

                }
                else
                {
                    response = new Message(FAILED, SUBSCRIPTION);
                }
        }
        response.setTransID(paymentNeededMsg.getTransID());
        sendToClient(response, clientConnection);
    }

    private static void handleLogout(Message msg, ConnectionToClient clientConnection) throws IOException, SQLException {
        Message logoutResponse = new Message();
        ArrayList<Object> data = new ArrayList<Object>();
        logoutResponse.setMessageType(MessageType.LOGOUT);
        logoutResponse.setTransID(msg.getTransID());
        logoutResponse.setDataType(PRIMITIVE);
        logoutResponse.addData("So long, and thanks for all the fish");
        SessionManager.dropSession(clientConnection);
    }

    private static void handleLogin(Message msg, ConnectionToClient clientConnection) throws IOException, SQLException{
        Message loginResponse;
        String email =(String) msg.getData().get(0);
        String pwd =(String) msg.getData().get(1);
        ParkingLot parkingLot = (ParkingLot) msg.getData().get(2);

        /*
        Now we check if user is an employee or not
         */
        User loggingUser;
        loggingUser = employeeController.getEmployeeByEmail(email);

        try
        {
            if (loggingUser != null //User IS an employee&
                    && parkingLot.getParkingLotID() == -1) //Employee trying to log in remotely
            {
                throw new LoginException("Employees can only log in to their workplace!");
            }

            if (loggingUser == null) //Not an employee...
            {
                loggingUser = customerController.getCustomerByEmail(email);
            }

            if (loggingUser == null || !loggingUser.getPassword().equals(pwd)) //User not found or wrong password.
            {
                throw new LoginException("Wrong Username or Password");
            }

            if (isUserAlreadyLoggedIn(email)) //login ok - checking if already logged in and not superuser
            {
                throw new LoginException(loggingUser.getName() + " is already logged on!");
            }
            // If you reached here, login is ok!
            Session session = new Session(clientConnection.hashCode(),
                    loggingUser, loggingUser.getUserType(), email, parkingLot);

            SessionManager.saveSession(clientConnection,session);
            loginResponse = new Message(MessageType.FINISHED, Message.DataType.SESSION, session.getSid(), session.getParkingLot(), session.getUserType(), session.getUser());
        }
        catch (LoginException le)
        {
            loginResponse = new Message(FAILED, Message.DataType.PRIMITIVE, le.getMessage());
        }
        loginResponse.setTransID(msg.getTransID());
        sendToClient(loginResponse, clientConnection);
    }

    private static boolean isUserAlreadyLoggedIn(String email) {
        for (Session session : SessionManager.getSessionsMap().values())
        {
            if (session.getEmail().equals(email))
            {
                return true;
            }
        }
        return false;
    }

    private static void handleUserQueries(Message queryMsg, Message response) throws SQLException
    {
        int userID = (int) queryMsg.getData().get(0);
        User.UserType type = (User.UserType) queryMsg.getData().get(1);
        switch (queryMsg.getDataType())
        {
            case PREORDER:
                response.setDataType(PREORDER);
                response.setData(customerController.getCustomersPreOrders(userID));
                break;
            case ORDER:
                Integer parkingLotID = (Integer) queryMsg.getData().get(2);
                response.setDataType(Message.DataType.ORDER);
                response.setData(customerController.getCustomersActiveOrdersInLot(userID, parkingLotID));
                break;
            case CARS:
                for (Integer car : (customerController.getCustomer((userID))).getCarIDList())
                {
                    response.addData(car);
                }
                break;
            case SUBSCRIPTION:
                response.setDataType(SUBSCRIPTION);
                response.addData(QUERY_RESPONSE);
                for (Object sub : customerController.getCustomer(userID).getSubscriptionList())
                {
                    response.addData(sub);
                }
                break;
            case COMPLAINT_PRE_CUSTOMER:
                response.setDataType(COMPLAINT_PRE_CUSTOMER);
                for (Complaint complaint : complaintController.getComplaintsByUserID(userID))
                {
                    response.addData(complaint);
                }
                break;
            case REPORT:
                response.setDataType(PRIMITIVE);
                ReportType reportType = (ReportType) queryMsg.getData().get(1);
                Integer parkingLotID2 = (Integer) queryMsg.getData().get(2);
                response.addData(reportController.generateReport(reportType,userID,parkingLotID2));
                break;
            case SESSION:
                break;
            default:
                response = new Message(MessageType.FAILED, PRIMITIVE, "Unknown query type " + queryMsg.getDataType());
        }
    }

    private static boolean handleQueries(Message queryMsg, ConnectionToClient clientConnection) throws IOException, SQLException {

        /**
         * Important!
         * In all queries, the first data object in the request will be the user requesting the query.
         */

        Message response = new Message();
        response.setDataType(queryMsg.getDataType());

        switch (queryMsg.getDataType())
        {
            case PARKING_LOT_LIST:
                ArrayList<Object> parkingLots = parkingController.getParkingLots();
                response.setDataType(Message.DataType.PARKING_LOT_LIST);
                response.setData(parkingLots);
                break;
            case PARKING_LOT_IMAGE:
                Integer parkingLotID = (Integer) queryMsg.getData().get(0);
                response.setDataType(PRIMITIVE);
                response.addData(parkingController.generateParkingStatusReport(parkingLotID));
                break;
            case PARKING_LOT:
                response.setDataType(PARKING_LOT);
                response.addData(parkingController.getParkingLotByID((Integer) queryMsg.getData().get(0)));
                break;
            case SINGLE_ORDER:
                response.setDataType(SINGLE_ORDER);
                response.addData(orderController.getOrder((Integer) queryMsg.getData().get(0)));
                break;
            case REPORT:
                handleUserQueries(queryMsg, response);
                break;
            case ALL_COMPLAINTS:
                response.setDataType(ALL_COMPLAINTS);
                response.setData(complaintController.getAllComplaint());
                break;
            case FINAL_REPORT:
                response.setDataType(FINAL_REPORT);
                for (FinalReport report : reportController.getAllReports()) {
                    response.addData(report);
                }
                break;
            default:
                if (queryMsg.getData().get(0) != null && queryMsg.getData().get(1) != null) //It's a user-based query
                {
                    handleUserQueries(queryMsg, response);
                }
        }
        response.setMessageType(FINISHED);
        response.setTransID(queryMsg.getTransID());
        sendToClient(response, clientConnection);
        return true;
    }

    private static void handleDeletion(Message deleteMsg, ConnectionToClient clientConnection) throws IOException, SQLException {

        Message response = new Message();
        switch(deleteMsg.getDataType())
        {
            case CARS:
                Integer uID = (Integer)deleteMsg.getData().get(0);
                Integer carToDelete = (Integer)deleteMsg.getData().get(1);
                response.setDataType(deleteMsg.getDataType());
                try{
                    if (customerController.removeCar(customerController.getCustomer(uID),carToDelete))
                    {//success
                        response.setMessageType(FINISHED);
                    }
                    else
                    {
                        response.setMessageType(FAILED);
                        response.setDataType(PRIMITIVE);
                        response.addData("Car removal from DB failed");
                    }
                }catch(LastCarRemovalException e)
                {
                    response.setMessageType(FAILED);
                    response.setDataType(PRIMITIVE);
                    response.addData("It is required to have at least 1 registered car");
                }

                break;
            case PREORDER:
                PreOrder preOrderToDelete = (PreOrder) deleteMsg.getData().get(0);
                Customer customer = customerController.getCustomer(preOrderToDelete.getCostumerID());
                Order removedOrder = customerController.removeOrder(customer, preOrderToDelete.getOrderID());
                if (removedOrder == null)
                { //TODO: Add support for payment required && order not found exception
                    response.setMessageType(FAILED);
                }
                else
                {
                    response = new Message(FINISHED, PREORDER, removedOrder);
                }
                break;
            case COMPLAINT_PRE_CUSTOMER:
                Complaint complaint = (Complaint) deleteMsg.getData().get(0);
                if (complaintController.cancelComplaint(complaint.getComplaintID()))
                    response = new Message(FINISHED, COMPLAINT_PRE_CUSTOMER);
                else
                    response = new Message(FAILED, PRIMITIVE, "Something went wrong!");
                break;
            default:
                response = new Message(FAILED, PRIMITIVE, "Unsupported creation type: " + deleteMsg.getMessageType());
        }
        response.setTransID(deleteMsg.getTransID());
        sendToClient(response,clientConnection);
    }

    private static boolean handleCreation(Message createMsg, ConnectionToClient clientConnection) throws Exception {
        Message createMsgResponse;
        switch(createMsg.getDataType())
        {
            case CUSTOMER:
                Customer customer = mapper.convertValue(createMsg.getData().get(0),Customer.class);
                Customer newCustomer = customerController.addNewCustomer(customer);
                createMsgResponse = new Message(FINISHED, Message.DataType.CUSTOMER, newCustomer);
                break;
            case ORDER:
                Order order = mapper.convertValue(createMsg.getData().get(0),Order.class);

                /*
                Check that the user isn't trying to order a 14-day long parking with a full subscription
                 */
                if (TimeUtils.timeDifference(order.getActualEntryTime(), order.getEstimatedExitTime(), TimeUtils.Units.DAYS) > 14
                        && (subscriptionController.getSubscriptionByCarAndType(order.getCostumerID(),
                        order.getCarID(),
                        Subscription.SubscriptionType.FULL) != null))
                {
                    createMsgResponse = new Message(FAILED, PRIMITIVE, "You can't park for more than 14 days with a Full Subscription!");
                    break;
                }
                Order entrance;
                Integer orderID = order.getOrderID();
                if (parkingController.checkCarAlreadyParked(order.getCarID()))
                {
                    throw new CustomerNotificationFailureException("You car is already parked in one of our lots!");
                }
                if (orderID == 0)//Entering parking normally
                {
                    entrance = customerController.addNewOrder(order);
                    orderID = entrance.getOrderID();
                }
                else
                {
                    entrance = orderController.getOrder(orderID);
                    entrance.setActualEntryTime(new Date());
                    customerController.updatePreOrderEntranceToParking(entrance);
                    entrance.setOrderStatus(IN_PROGRESS);
                }
                parkingController.enterParkingLot(orderID);
                createMsgResponse = new Message(FINISHED, Message.DataType.ORDER, entrance);
                break;
            case PREORDER:
                PreOrder preorder = mapper.convertValue(createMsg.getData().get(0), PreOrder.class);
                /*
                Check that the user isn't trying to order a 14-day long parking with a full subscription
                 */
                if (TimeUtils.timeDifference(preorder.getEstimatedEntryTime(), preorder.getEstimatedExitTime(), TimeUtils.Units.DAYS) > 14
                        && (subscriptionController.getSubscriptionByCarAndType(preorder.getCostumerID(),
                        preorder.getCarID(),
                        Subscription.SubscriptionType.FULL) != null))
                {
                    createMsgResponse = new Message(FAILED, PRIMITIVE, "You can't park for more than 14 days with a Full Subscription!");
                    break;
                }

                double estimatedCharge = orderController.handleBillingUponPreOrderPlacement(preorder.getCostumerID(), preorder);
                if (estimatedCharge > 0)
                {
                    preorder.setCharge(estimatedCharge);
                    SessionManager.getSession(clientConnection).setOrderInNeedOfPayment((PreOrder) preorder);
                    createMsgResponse = new Message(NEED_PAYMENT, PRIMITIVE, estimatedCharge);
                }
                else
                {
                    Order newPreOrder = customerController.addNewPreOrder(preorder);
                    createMsgResponse = new Message(FINISHED, PREORDER, newPreOrder);
                }
                break;
            case PARKING_LOT:
                Integer parkingLotID = (Integer) createMsg.getData().get(0);
                parkingController.initiateParkingLot(parkingLotID);
                createMsgResponse = new Message(FINISHED, PARKING_LOT);
                break;
            case CARS:
                Integer uID = (Integer)createMsg.getData().get(0);
                Integer carToAdd = (Integer)createMsg.getData().get(1);
                Customer customerToAddTo = customerController.getCustomer(uID);
                if (customerToAddTo != null)
                {
                    customerController.addCar(customerToAddTo, carToAdd);
                }
                createMsgResponse = new Message(FINISHED, Message.DataType.CARS, carToAdd);
                break;
            case SUBSCRIPTION:
                Subscription subscription;
                if (createMsg.getData().get(0) instanceof RegularSubscription)
                {
                    subscription = (RegularSubscription)createMsg.getData().get(0);
                }
                else
                {
                    subscription = (FullSubscription)createMsg.getData().get(0);
                }
                double subscriptionCharge = billingController.calculateChargeForSubscription(subscription);
                SessionManager.getSession(clientConnection).setSubscriptionInNeedOfPayment(subscription);
                createMsgResponse = new Message(NEED_PAYMENT, PRIMITIVE, subscriptionCharge);
                break;
            case COMPLAINT_PRE_CUSTOMER:
                Complaint incomingComplaint = (Complaint) createMsg.getData().get(0);
                Complaint returnComplaint = complaintController.createComplaint(incomingComplaint);
                if (returnComplaint != null)
                    createMsgResponse = new Message(FINISHED, COMPLAINT_PRE_CUSTOMER, returnComplaint);
                else
                    createMsgResponse = new Message(FAILED, PRIMITIVE, "Ironically, something went wrong with your request.");
                break;
            default:
                createMsgResponse = new Message(FAILED, PRIMITIVE,"Unknown Type: " + createMsg.getDataType().toString());
                createMsgResponse.setTransID(createMsg.getTransID());
                sendToClient(createMsgResponse, clientConnection);
                return false;
        }
        createMsgResponse.setTransID(createMsg.getTransID());
        sendToClient(createMsgResponse, clientConnection);
        return true;
    }

    private static boolean handleUpdate(Message updateMsg, ConnectionToClient clientConnection) throws IOException, SQLException {
        Message returnMessage = new Message();
        switch (updateMsg.getDataType()) {
            case PARKING_SPACE:
                Integer parkingLotID = (Integer) updateMsg.getData().get(0);
                int i;
                for (i = 1; i < updateMsg.getData().size(); i++) {
                    ParkingSpace space = (ParkingSpace) updateMsg.getData().get(i);
                    if (dbController.updateParkingSpace(parkingLotID, space)) //If successful
                    {
                        parkingController.setParkingSpaceStatus(
                                parkingLotID, space.getStatus(),
                                space.getWidth(),
                                space.getHeight(),
                                space.getDepth());
                    }
                }
                returnMessage = new Message(FINISHED, PRIMITIVE, i + " parking spaces updated!");
                break;
            case COMPLAINT_PRE_CUSTOMER:
                Complaint complaint = (Complaint) updateMsg.getData().get(0);
                switch (complaint.getStatus())
                {
                    case ACCEPTED:
                        complaintController.acceptComplaint(complaint.getComplaintID());
                        break;
                    case REJECTED:
                        complaintController.rejectComplaint(complaint.getComplaintID());
                        break;
                    case CANCELLED:
                        complaintController.cancelComplaint(complaint.getComplaintID());
                        break;
                }
                returnMessage = new Message(FINISHED, COMPLAINT_PRE_CUSTOMER);
                break;
            case PARKING_LOT:
                Integer parkingLotIDToUpdate = (Integer) updateMsg.getData().get(0);
                boolean isFull = (boolean) updateMsg.getData().get(1);
                parkingController.getParkingLotByID(parkingLotIDToUpdate).setIsFullState(isFull);
                //TODO: Should we have a DB call for this as well?
                returnMessage = new Message(FINISHED, PRIMITIVE, "Done.");
        }
        returnMessage.setTransID(updateMsg.getTransID());
        sendToClient(returnMessage, clientConnection);
        return true;
    }
}
