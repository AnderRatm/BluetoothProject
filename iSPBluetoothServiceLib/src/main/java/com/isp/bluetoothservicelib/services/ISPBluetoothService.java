package com.isp.bluetoothservicelib.services;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;


@SuppressLint("NewApi")
public class ISPBluetoothService {

	private static final String NAME_SECURE = "ISPConnectionSecure";
	private static final String NAME_INSECURE = "ISPConnectionInsecure";
	
	// Unique UUID for this application
    private static final UUID MY_UUID_SECURE =
        UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
    private static final UUID MY_UUID_INSECURE =
        UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");
    
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
    
	private BluetoothAdapter mAdapter;
    private Handler mHandler;
    private AcceptThread mSecureAcceptThread;
    private AcceptThread mInsecureAcceptThread;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    
    private int mState;
    
    //Constantes para indicar o estado atual da A�o
    public static final int STATE_NONE = 0; //N�o faz nada
    public static final int STATE_LISTEN = 1; //Ouvindo conex�es
    public static final int STATE_CONNECTING = 2; //Iniciando uma conex�o
    public static final int STATE_CONNECTED = 3; //Conectado
    
    //Variaveis que ir�o dizer para o handler que chamou essa conex�o
    //o que est� acontecendo no momento, exemplo: Nome do dispositivo,
    //mensagem lida, mensagem enviada
    private int messageStateChange = 1;
    private int messageRead = 2;
    private int messageWrite = 3;
    private int messageDeviceName = 4;
    private int messageToast = 5;
    
    //Chaves para o handler
    private String deviceName = "device_name";
    private String toast = "toast";
    
    private static ISPBluetoothService me;
    
    public synchronized static ISPBluetoothService getInstance(){
    	if(me == null){
    		me = new ISPBluetoothService();
    	}
    	return me;
    }
    
    private ISPBluetoothService(){
    }

    public void init(Handler handler, int messageStateChange, int messageRead, int messageWrite,
    		int messageDeviceName, int messageToast, String deviceName, String toast){
    	mAdapter = BluetoothAdapter.getDefaultAdapter();
    	mState = STATE_NONE;
    	this.mHandler = handler;
    	this.messageStateChange = messageStateChange;
    	this.messageRead = messageRead;
    	this.messageWrite = messageWrite;
    	this.messageDeviceName = messageDeviceName;
    	this.messageToast = messageToast;
    	this.deviceName = deviceName;
    	this.toast = toast;
    }
    
    private ISPBluetoothService(Context context, Handler handler){
    	mAdapter = BluetoothAdapter.getDefaultAdapter();
    	mState = STATE_NONE;
    	mHandler = handler;
    }
    
    private ISPBluetoothService(Handler handler, int messageStateChange, int messageRead, int messageWrite,
    		int messageDeviceName, int messageToast, String deviceName, String toast){
    	mAdapter = BluetoothAdapter.getDefaultAdapter();
    	mState = STATE_NONE;
    	this.mHandler = handler;
    	this.messageStateChange = messageStateChange;
    	this.messageRead = messageRead;
    	this.messageWrite = messageWrite;
    	this.messageDeviceName = messageDeviceName;
    	this.messageToast = messageToast;
    	this.deviceName = deviceName;
    	this.toast = toast;
    }
    
    private synchronized void setState(int state){
    	mState = state;
    	mHandler.obtainMessage(messageStateChange, state, -1).sendToTarget();
    }
    
    public synchronized int getState(){
    	return mState;
    }
    
    //Inicia todo mundo
    public synchronized void start() {

        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

        setState(STATE_LISTEN);

        // Start the thread to listen on a BluetoothServerSocket
        if (mSecureAcceptThread == null) {
            mSecureAcceptThread = new AcceptThread(true);
            mSecureAcceptThread.start();
        }
        if (mInsecureAcceptThread == null) {
            mInsecureAcceptThread = new AcceptThread(false);
            mInsecureAcceptThread.start();
        }
    }
    
    /**
     * P�ra todas as threads
     */
    public synchronized void stop() {
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        if (mSecureAcceptThread != null) {
            mSecureAcceptThread.cancel();
            mSecureAcceptThread = null;
        }

        if (mInsecureAcceptThread != null) {
            mInsecureAcceptThread.cancel();
            mInsecureAcceptThread = null;
        }
        setState(STATE_NONE);
    }
    
    /**
     * Write to the ConnectedThread in an unsynchronized manner
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    public void write(byte[] out) {
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState != STATE_CONNECTED) return;
            r = mConnectedThread;
        }
        // Perform the write unsynchronized
        r.write(out);
    }
    
    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    private void connectionFailed() {
        // Send a failure message back to the Activity
        Message msg = mHandler.obtainMessage(messageToast);
        Bundle bundle = new Bundle();
        bundle.putString(toast, "N�o foi poss�vel conectar");
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        // Start the service over to restart listening mode
        ISPBluetoothService.this.start();
    }
    
    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private void connectionLost() {
        // Send a failure message back to the Activity
        Message msg = mHandler.obtainMessage(messageToast);
        Bundle bundle = new Bundle();
        bundle.putString(toast, "Device connection was lost");
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        // Start the service over to restart listening mode
        ISPBluetoothService.this.start();
    }
    
    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     * @param device  The BluetoothDevice to connect
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     */
    public synchronized void connect(BluetoothDevice device, boolean secure) {
        // Cancel any thread attempting to make a connection
        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(device, secure);
        mConnectThread.start();
        setState(STATE_CONNECTING);
    }
    
    //M�todo que vai iniciar a thread para manter a conex�o (ConnectedThread)
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device, final String socketType){
    	//Cancelando a thread de conex�o, caso a conex�o j� esteja estabelecida
    	if(mConnectThread != null){
    		mConnectThread.cancel();
    		mConnectThread = null;
    	}
    	
    	//Cancelando a thread que est� rodando a conex�o
    	if(mConnectedThread != null){
    		mConnectedThread.cancel();
    		mConnectedThread = null;
    	}
    	
    	//Cancelando a thread que inicia tudo, para manter apenas um dispositivo conectado
    	if(mSecureAcceptThread != null){
    		mSecureAcceptThread.cancel();
    		mSecureAcceptThread = null;
    	}
    	if(mInsecureAcceptThread != null){
    		mInsecureAcceptThread.cancel();
    		mInsecureAcceptThread = null;
    	}
    	
    	//Iniciando a thread para gerenciar a conex�o
    	mConnectedThread = new ConnectedThread(socket, socketType);
    	mConnectedThread.start();
    	
    	//Mandando o nome do dispotivo conectado para a atividade que gerencia a tela
    	Message msg = mHandler.obtainMessage(messageDeviceName);
    	Bundle bundle = new Bundle();
    	bundle.putString(deviceName, device.getName());
    	msg.setData(bundle);
    	mHandler.sendMessage(msg);
    	setState(STATE_CONNECTED);
    }
    
    //thread para escutar conex�es
    private class AcceptThread extends Thread{
    	
    	//Server socket local
    	private final BluetoothServerSocket mmServerSocket;
    	private String mSocketType;
    	
    	public AcceptThread(boolean secure){
    		BluetoothServerSocket tmp = null;
    		mSocketType = secure ? "Secure" : "Insecure";
    		
    		//Criando o server socket que ir� escutar conex�es
    		try {
				if(secure){
					tmp = mAdapter.listenUsingRfcommWithServiceRecord(NAME_SECURE, MY_UUID);
				} else {
					tmp = mAdapter.listenUsingInsecureRfcommWithServiceRecord(NAME_INSECURE, MY_UUID_INSECURE);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
    		mmServerSocket = tmp;
    	}
    	
    	@Override
    	public void run() {
    		setName("AcceptThread" + mSocketType);
    		BluetoothSocket socket = null;
    		
    		//Escutando o servidor, se n�o estiver conectado
    		while(mState != STATE_CONNECTED){
    			try {
					//Chamada de bloqueio. Ir� retornar se uma 
    				//conex�o for bem sucedida, ou exce��o
    				socket = mmServerSocket.accept();
				} catch (Exception e) {
					e.printStackTrace();
					break;
				}
    			
    			//Se a conex�o for aceita
    			if(socket != null){
    				synchronized (ISPBluetoothService.this) {
						switch (mState) {
						case STATE_LISTEN:
						case STATE_CONNECTING:
							//Inicia a thread de conex�o
							connected(socket, socket.getRemoteDevice(), mSocketType);
							break;
						case STATE_NONE:
						case STATE_CONNECTED:
							try {
								socket.close();
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
    			}
    			
    		}
    	}
    	
    	public void cancel(){
    		try {
				mmServerSocket.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
    	}
    	
    }
    
    //Thread para tentar fazer uma conex�o com outro dispositivo
    private class ConnectThread extends Thread{
    	
    	private final BluetoothSocket mmSocket;
    	private final BluetoothDevice mmDevice;
    	private String mSocketType;
    	
    	public ConnectThread(BluetoothDevice device, boolean secure){
    		mmDevice = device;
    		BluetoothSocket tmp = null;
    		mSocketType = secure ? "Secure" : "Insecure";
    		
    		//Pegando o BluetoothSocket para a conex�o com o dispositivo
    		try {
				if(secure){
					tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
				} else {
					tmp = device.createInsecureRfcommSocketToServiceRecord(MY_UUID_INSECURE);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
    		mmSocket = tmp;
    	}
    	
    	@Override
    	public void run() {
    		setName("ConnectThread" + mSocketType);
    		mAdapter.cancelDiscovery();
    		try {
				mmSocket.connect();
			} catch (Exception e) {
				//Fechando o socket
				try {
					mmSocket.close();
				} catch (Exception e2) {
					e.printStackTrace();
				}
				connectionFailed();
		    	return;
			}
    		synchronized (ISPBluetoothService.this) {
				mConnectThread = null;
			}
    		
    		//Iniciar a thread de conex�o conclu�da
    		connected(mmSocket, mmDevice, mSocketType);
    	}
    	
    	public void cancel(){
    		try {
				mmSocket.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
    	}
    	
    }
    
    //Thread que vai manter a conex�o viva com outro dispositivo
    private class ConnectedThread extends Thread{
    	private final BluetoothSocket mmSocket;
    	private final InputStream mmInStream;
    	private final OutputStream mmOutStream;
    	
    	public ConnectedThread(BluetoothSocket socket, String socketType){
    		mmSocket = socket;
    		InputStream tmpIn = null;
    		OutputStream tmpOut = null;
    		
    		//Pegar outputstream e inputstream do socket
    		try {
				tmpIn = socket.getInputStream();
				tmpOut = socket.getOutputStream();
			} catch (Exception e) {
				e.printStackTrace();
			}
    		mmInStream = tmpIn;
    		mmOutStream = tmpOut;
    	}
    	
    	@Override
    	public void run() {
    		byte[] buffer;
    		int bytes;
    		
    		while(true){
    			try {
    				//buffer = new byte[1024];
    				buffer = new byte[1];
					//Lendo do inputstream
    				bytes = mmInStream.read(buffer);
					
    				//Mandando os bytes obtidos para a atividade que controla as telas
    				mHandler.obtainMessage(messageRead, bytes, -1, buffer).sendToTarget();
				} catch (Exception e) {
					e.printStackTrace();
					connectionLost();
					ISPBluetoothService.this.start();
					break;
				}
    		}
    	}
    	
    	public void write(byte[] buffer){
    		try {
				mmOutStream.write(buffer);
				
				//Mandando a mensagem de volta para a atividade que controla as telas
				mHandler.obtainMessage(messageWrite, -1, -1, buffer).sendToTarget();
			} catch (Exception e) {
				e.printStackTrace();
			}
    	}
    	
    	public void cancel(){
    		try {
				mmSocket.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
    	}
    	
    }
    
    
}
