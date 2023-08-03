package it.unipi.iot;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class RemoteControlApplication implements Runnable {

    // S1 -> A1
    // S2,S3 -> A2
    // S2,S3 -> A3

    // tropomina = 0.1 g/ml if val > 1 allora pericolo
    private static final float DNG_TRP_TH = 0.1f;
    private static final float CTR_TRP_TH = 0.07f;

    private static final float DNG_OX_TH = 49.9f;
    private static final float CTR_OX_TH = 59.9f;

    private static final int U_DNG_HB_TH = 250;
    private static final int U_CTR_HB_TH = 220;
    private static final int L_DNG_HB_TH = 20;
    private static final int L_CTR_HB_TH = 30;

    private static final RemoteControlApplication instance = new RemoteControlApplication();

    String[] attuatori = new String[3];
    static HashMap<String, String[]> pazienti = new HashMap<>();

    public static RemoteControlApplication getInstance() {
        return instance;
    }

    public int retrieveSensorType(String id)
    {
        if (id.startsWith("O"))
        {
            return 0;
        }
        else if (id.startsWith("T"))
        {
            return 1;
        } 
        else // Vuol dire che è C
        {
            return 2;
        }
    }

    public String retrieveActuatorType(int index) 
    {
        String type = "";
        switch (index) 
        {
            case 0:
                type = "mask";
                break;
            case 1:
                type = "medicine";
                break;
            case 2:
                type = "defibrillator";
                break;
            default:
                System.out.println("Invalid control value!");
                return "";
        }
        return type;
    }

    public String retrieveActuatorIP(String patient_id, int index) throws SQLException 
    {
        // String type = retrieveActuatorType(index);
        String ip = DB.retrieveActuatorIP(retrieveActuatorType(index));
        if (!ip.isEmpty()) 
        {
            // Ip recuperato -> lo inserisco nella tabella
            pazienti.get(patient_id)[index] = ip;
            return ip;
        }
        else
        {
            // Vuol dire che non ce l'ip nel DB
            System.out.println("ERRORE: IP NON PRESENTE NEL DB\n");
            return "";
        }
    }

    public boolean changeActuatorStatus(String patient_id, int index, int isActive) throws SQLException {
        String[] newIps = pazienti.get(patient_id);
        // Recupero l'ip
        String actuatorIp = "";
        if (pazienti.get(patient_id)[index].isEmpty())
            actuatorIp = DB.retrieveActuatorIP(retrieveActuatorType(index));
        else
            actuatorIp = pazienti.get(patient_id)[index];
        if (actuatorIp.isEmpty()) {
            // Non ha trovato l'ip 
            System.out.println("ERROR: IP NOT FOUND FOR PATIENT " + patient_id);
            return false;
        }
        
        final int MAX_ATTEMPTS = 3;
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            try {
                // Richiedo la PUT
                if (Actuator_Client.putClientRequest(actuatorIp, retrieveActuatorType(index), isActive)) {
                    DB.updateActuatorStatus(actuatorIp, patient_id, isActive == 0 ? false : true);
                    if (isActive != 0) {
                        // Va attivato
                        // Lo aggiungo alla mappa
                        newIps[index] = actuatorIp;
                        pazienti.put(patient_id, newIps);
                    } else {
                        newIps[index] = "";
                        pazienti.put(patient_id, newIps);
                    }
                    return true;
                } else {
                    // Errore nella PUT
                    System.out.println("ERROR IN PUT REQUEST, ATTEMPT " + (attempt + 1));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    
        return false;
    }
    

    public int checkCardio(float cardioValue)
    {
        if(cardioValue>=U_DNG_HB_TH || cardioValue<=L_DNG_HB_TH)
            return 2; // pericoloso
        else if(cardioValue>=U_CTR_HB_TH || cardioValue<=L_CTR_HB_TH)
            return 1;
        else
            return 0;
    }

    public int checkTroponin(float trpValue)
    {
        if(trpValue>=DNG_TRP_TH)
            return 2; // pericoloso
        else if(trpValue>=CTR_TRP_TH)
            return 1;
        else
            return 0;
    }

    public int calculateDanger(float trpValue, float cardioValue)
    {
        int trpLevel = checkTroponin(trpValue);
        int cardioLevel = checkCardio(cardioValue);
        /*int sum = trpLevel + cardioLevel;
        if(sum==4)
        {
            // 1. 2-2 -> 2
            return 2;
        }
        else if(sum==3 || sum==2)
        {
            // 2. 1-1 -> 1
            // 3. 1-2 -> 1
            // 4. 2-1 -> 1
            // 5. 0-2 -> 1
            // 6. 2-0 -> 1
            return 1;
        }
        else if(sum==1 || sum==0)
        {
            // 7. 0-0 -> 0 
            // 8. 0-1 -> 0
            // 9. 1-0 -> 0
            return 0;
        }
        return -1;*/
        return trpLevel + cardioLevel;
    }

    public void run() 
    {
        
        try 
        {
            pazienti = DB.retrieveActiveActuators();
            List<String> typeList = Arrays.asList("oxygen_sensor", "troponin_sensor", "cardio_sensor");
            HashMap<String, Float> retrieved;

            for (String s : typeList) {
                // Recupero i dati del sensore s dal DB
                retrieved = new HashMap<>(DB.retrieveSensorData(s));

                if (retrieved.isEmpty()) 
                {
                    System.out.println("NESSUN DATO RECUPERATO\n");
                } 
                else 
                {
                    for (String key : retrieved.keySet()) 
                    {
                        // Tolgo la prima lettera che corrisponde al tipo di sensore
                        String patient_id = key.substring(1, key.length());
                        int index = retrieveSensorType(key);

                        // Controllo che tipo di dato abbiamo
                        if(index==0) // OSSIGENO
                        {
                            // Analizziamo il valore del ossigeno
                            int value = retrieved.get(key) <= DNG_OX_TH ? 2 : retrieved.get(key) <= CTR_OX_TH ? 1 : 0;
                            try 
                            {
                                if(changeActuatorStatus(patient_id, index, value))
                                {
                                    System.out.println("ATTUATORE ON LIVELLO "+ value +"\n");
                                }
                                else
                                {
                                    return;
                                }
                            }
                            catch (SQLException e)
                            {
                                e.printStackTrace();
                            }
                        }
                        else if(index==1)
                        {
                            float trpValue = retrieved.get("k");
                            float cardioValue = retrieved.get("c");
                            int value = calculateDanger(trpValue, cardioValue);
                            if(value == 0 || value >= 3){
                                try 
                                {
                                    if(changeActuatorStatus(patient_id, index, value) && changeActuatorStatus(patient_id, index + 1, value))
                                    {
                                        System.out.println("ATTUATORI ON LIVELLO "+ value +"\n");
                                    }
                                    else
                                    {
                                        return;
                                    }
                                }
                                catch (SQLException e)
                                {
                                    e.printStackTrace();
                                }
                            }
                            else {
                                try 
                                {
                                    if(changeActuatorStatus(patient_id, index, value))
                                    {
                                        System.out.println("ATTUATORE ON LIVELLO "+ value +"\n");
                                    }
                                    else
                                    {
                                        return;
                                    }
                                }
                                catch (SQLException e)
                                {
                                    e.printStackTrace();
                                }
                            }
                        }
                        else if(index == 2){
                            continue;
                        }
                        else
                        {
                            // NON CI DEVE ARRIVARE 
                            System.out.println("SENSORE NON RICONOSCIUTOOO\n");
                        }
                    }
                }
            }
        } 
        catch (SQLException e) 
        {
            e.printStackTrace();
        } 
        finally 
        {
            Thread.currentThread().interrupt();
        }

    }

    public static void main(String[] args) 
    {
        Thread thread = new Thread(new RemoteControlApplication());
        thread.start();
    }

}
