package it.unipi.iot;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.eclipse.paho.client.mqttv3.*;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.nio.charset.StandardCharsets;

import java.net.InetAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Coordinator implements MqttCallback, Runnable {

    private static final String MQTT_BROKER = "tcp://[::1]:1883";
    private static final String OXYGEN_TOPIC = "oxygen";
    private static final String CARDIO_TOPIC = "cardio";
    private static final String TROPONIN_TOPIC = "troponin";

    private MqttClient mqttClient;
    private CoapServer coapServer;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    public static class MyCoapResource extends CoapResource {
        public MyCoapResource(String name) {
            super(name);
        }

        @Override
        public void handlePOST(CoapExchange exchange) {
            System.out.println("STARTING HANDLE POST\n");

            String s = new String(exchange.getRequestPayload());
            JSONObject obj;
            JSONParser parser = new JSONParser();

            try {
                obj = (JSONObject) parser.parse(s);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }

            InetAddress address = exchange.getSourceAddress();
            String ip = address.toString().substring(1); // Removes initial slash
            String type = (String) obj.get("type");

            // Using DB class method to register actuator
            DB.replaceActuator(ip, type);
            System.out.println("Actuator registered successfully.");
            exchange.respond(ResponseCode.CREATED);
        }

        @Override
        public void handleDELETE(CoapExchange exchange) {
            System.out.println("STARTING HANDLE DELETE\n");

            InetAddress address = exchange.getSourceAddress();
            String ip = address.toString().substring(1); // Removes initial slash

            // Using DB class method to delete actuator
            DB.deleteActuator(ip);
            System.out.println("Actuator deleted successfully.");
            exchange.respond(ResponseCode.DELETED);
        }
    }

    public Coordinator() {
        this.coapServer = new CoapServer(5683);
        this.coapServer.add(new MyCoapResource("registration"));
        executorService.execute(this.coapServer::start);
    }

    @Override
    public void run() {
        this.mqttClient = this.connectToBroker();
        this.subscribeToTopics();
    }

    private MqttClient connectToBroker() {
        String clientId = "tcp://127.0.0.1:1883";
        try {
            MqttClient mqttClient = new MqttClient(MQTT_BROKER, clientId);
            mqttClient.setCallback(this);
            mqttClient.connect();
            return mqttClient;
        } catch (MqttException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void subscribeToTopics() {
        try {
            this.mqttClient.subscribe(OXYGEN_TOPIC);
            this.mqttClient.subscribe(CARDIO_TOPIC);
            this.mqttClient.subscribe(TROPONIN_TOPIC);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void connectionLost(Throwable throwable) {
        System.out.println("CONNECTION LOST");
        throwable.printStackTrace();
    }

    public void deliveryComplete(IMqttDeliveryToken token) {
        System.out.println("DELIVERY COMPLETATA!");
    }

    public void messageArrived(String topic, MqttMessage message) throws Exception {
        //System.out.println("MESSAGE ARRIVED - Topic: " + topic + ", Payload: " + message);

        if (topic.equals(OXYGEN_TOPIC) || topic.equals(CARDIO_TOPIC) || topic.equals(TROPONIN_TOPIC)) {
            String payload = new String(message.getPayload(), StandardCharsets.UTF_8);
            JsonObject jsonPayload = JsonParser.parseString(payload).getAsJsonObject();
            String sensorId = jsonPayload.get("id").getAsString();
            float value = jsonPayload.get("value").getAsFloat();
            if(sensorId.startsWith("t"))
            {
                value = value / 100;
            }

            String tableName = "";
            switch (topic) {
                case OXYGEN_TOPIC:
                    tableName = "oxygen_sensor";
                    break;
                case CARDIO_TOPIC:
                    tableName = "cardio_sensor";
                    break;
                case TROPONIN_TOPIC:
                    tableName = "troponin_sensor";
                    break;
            }

            DB.insertSensorData(tableName, sensorId, value);
        }
    }

    public static void main(String[] args) {
        Coordinator coordinator = new Coordinator();
        Thread coordinatorThread = new Thread(coordinator);
        coordinatorThread.start();
    }
}