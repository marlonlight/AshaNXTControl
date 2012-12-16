/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.bluetooth.*;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;
import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;
import javax.microedition.sensor.*;

/**
 * @author mluz
 */
public class AshaNXTControl extends MIDlet implements CommandListener, DiscoveryListener, DataListener{
    
    public Display display = Display.getDisplay(this);
    public List menu;
    public List devices;
    public Command cmdExit = new Command("Exit", Command.EXIT, 1);
    public Command cmdBack = new Command("Back", Command.BACK, 1);
    
    MyCanvas canvas = new MyCanvas();
    
    SensorConnection sensorConnection;
    private static final int BUFFER_SIZE = 3;
    
    
    //bluetooth elements
    LocalDevice m_LclDevice;
    DiscoveryAgent m_DscrAgent;
    StreamConnectionNotifier m_StrmNotf;
    StreamConnection m_StrmConn;
    OutputStream m_Output;
    InputStream  m_Input;
    
    String NXTAddress;
    
    boolean isConnect = false;
    
    public void startApp() {
        try {
            menu = new List("Asha NXT Control", List.IMPLICIT);
            menu.append("search NXT", null);
            menu.append("go", null);
            menu.append("turn Right", null);
            menu.append("turn Left", null);
            menu.append("stop", null);
            menu.addCommand(cmdExit);
            menu.setCommandListener(this);
            display.setCurrent(menu);
            
            
            SensorInfo infos[];
            infos = SensorManager.findSensors("acceleration", null);
            int datatypes[] = new int[infos.length];
            int i = 0;
            String sensor_url = "";
            boolean sensor_found = false;
            while (!sensor_found) {
                datatypes[i] = infos[i].getChannelInfos()[0].getDataType();
                if (datatypes[i] == ChannelInfo.TYPE_DOUBLE) {
                    sensor_url = infos[i].getUrl();
                    sensor_found = true;
                }
                    else i++;
            }      
            sensorConnection = (SensorConnection) Connector.open(sensor_url);
            sensorConnection.setDataListener(this, BUFFER_SIZE);
            
        } catch (Exception ex) {
            
        }
        
    }
    
    public void pauseApp() {
    }
    
    public void destroyApp(boolean unconditional) {
    }

    public void commandAction(Command c, Displayable d) {
        if (c == List.SELECT_COMMAND) {
            if (menu.getSelectedIndex() == 0) {
                searchNXT();
            } else if (menu.getSelectedIndex() == 1) {
                go();
            } else if (menu.getSelectedIndex() == 2) {
                turnRight();
            } else if (menu.getSelectedIndex() == 3) {
                turnLeft();
            } else if (menu.getSelectedIndex() == 4) {
                stop();
            }
        } else if (c == cmdExit) {
            notifyDestroyed();
        } else if (c == cmdBack) {
            display.setCurrent(menu);   
        }
    }

    private void connect() {
        try {
            m_LclDevice.setDiscoverable(DiscoveryAgent.GIAC);
            String m_strUrl= "btspp://" + NXTAddress +
            ":1;authenticate=false;encrypt=false";


            m_StrmConn = (StreamConnection)Connector.open(m_strUrl);
            m_Output = m_StrmConn.openOutputStream();
            m_Input = m_StrmConn.openInputStream();
            
            display.setCurrent(canvas);
            isConnect = true;

        } catch (Exception ex) {
            showError("connect:"+ex.getClass().getName());
        }
    }

    public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
        try {
            String deviceName = btDevice.getFriendlyName(true);
            if (deviceName.indexOf("NXT") >=0) {
                //have found NXT
                devices.append(deviceName, null);
                NXTAddress = btDevice.getBluetoothAddress();
            }

        } catch (IOException ex) {
            
        }
    }

    public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
        
    }

    public void serviceSearchCompleted(int transID, int respCode) {
        
    }

    public void inquiryCompleted(int discType) {
        if (discType == DiscoveryListener.INQUIRY_COMPLETED) {
            connect();
        }
    }

    private void searchNXT() {
        devices = new List("BT Devices", List.IMPLICIT);
        display.setCurrent(devices);
        devices.addCommand(cmdBack);
        devices.setCommandListener(this);
        try {
            //First, get the local device and obtain the discovery agent.
            m_LclDevice = LocalDevice.getLocalDevice();

            m_DscrAgent=  m_LclDevice.getDiscoveryAgent();

            m_DscrAgent.startInquiry(DiscoveryAgent.GIAC,this);
        } catch (BluetoothStateException ex) {
            showError("searchNXT:"+ex.getMessage());
        }        
    }

    private void showError() {
        showError("AI");
    }
    private void showError(String msg) {
        Alert alert = new Alert("error");
        alert.setString(msg);
        alert.setTimeout(Alert.FOREVER);
        display.setCurrent(alert);
    }
    
    private void go(){
        byte startMotorsACmd[] = {
        12, 0, -128, 4, 0, 70, 7, 0, 0, 32, 
        0, 0, 0, 0};
        
        byte startMotorsCCmd[] = {
        12, 0, -128, 4, 2, 70, 7, 0, 0, 32, 
        0, 0, 0, 0};
        
        sendCommand(startMotorsCCmd);
        sendCommand(startMotorsACmd);
    }
    
    private void turnRight(){
        byte startMotorsACmd[] = {
        12, 0, -128, 4, 0, 30, 7, 0, 0, 32, 
        0, 0, 0, 0};

        byte startMotorsCCmd[] = {
        12, 0, -128, 4, 2, 70, 7, 0, 0, 32, 
        0, 0, 0, 0};

        sendCommand(startMotorsCCmd);
        sendCommand(startMotorsACmd);
        
    }
    
    private void turnLeft(){
        byte startMotorsACmd[] = {
        12, 0, -128, 4, 0, 70, 7, 0, 0, 32, 
        0, 0, 0, 0};

        byte startMotorsCCmd[] = {
        12, 0, -128, 4, 2, 30, 7, 0, 0, 32, 
        0, 0, 0, 0};

        sendCommand(startMotorsCCmd);
        sendCommand(startMotorsACmd);
        
    }
    
    private void stop(){
        byte stopAllMotorsCmd[] = {
            12, 0, -128, 4, -1, 0, 1, 0, 0, 32, 
            0, 0, 0, 0};
        sendCommand(stopAllMotorsCmd);
    }

    private void sendCommand(byte[] data) {
        try {
            m_Output.write(data);
            m_Output.flush();
        } catch (IOException ex) {
            showError("sendCommand:" + ex.getMessage());
        }
    }
    
    public void dataReceived(SensorConnection sensor, Data[] dataArray, boolean isDataLost) {
        if (dataArray == null) {
                return;
        }
        if (dataArray.length < 3) {
                return;
        }
        
        double temp[] = getIntegerDirections(dataArray);
        if (isConnect){
            if (canvas.isPressed) {
            if (((int)temp[1]) < -2) {
                turnLeft();
            } else if (((int)temp[1]) > 2) {
                turnRight();
            } else {
                go();
            }
        } else {
            stop();
        }        
        }
    }
    
    private static double[] getIntegerDirections(Data[] data) {
        double [][] intValues = new double[3][BUFFER_SIZE];
        double[] directions = new double[3];
        for (int i=0; i<3; i++){
            intValues[i] = data[i].getDoubleValues();
            double temp = 0;
            for (int j = 0; j<BUFFER_SIZE; j++) {
                    temp = temp + intValues[i][j];
            }
            directions[i] = temp/BUFFER_SIZE;
        }
        return directions;
    }
    
}
