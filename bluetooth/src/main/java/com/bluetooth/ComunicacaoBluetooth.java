package com.bluetooth;

import java.io.DataOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.text.NumberFormat;
import java.util.Timer;
import java.util.TimerTask;

import android.app.ActionBar;
import android.app.Activity;

import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.ImageFormat;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bluetooth.R;
import com.isp.bluetoothservicelib.services.ISPBluetoothService;


public class ComunicacaoBluetooth extends Activity implements Runnable {

	private BluetoothDevice device;

	private RelativeLayout layoutPaciente;
	private RelativeLayout layoutTratamento;
	private RelativeLayout layoutEquipamento;
	private RelativeLayout layoutDetalhe;

	// Variaveis e private static final int antes para comunica��o bluetooth
	private byte[] pacote;
	private ISPBluetoothService service;
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;
	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";
	private String mConnectedDeviceName = null;

	private Handler handlerSerial;
	private static final int PACOTE_START = 0;
	private static final int PACOTE_ENDERECO_DESTINO_HIGH = 1;
	private static final int PACOTE_ENDERECO_DESTINO_LOW = 2;
	private static final int PACOTE_CONFIG = 3;
	private static final int PACOTE_TAMANHO_HIGH = 4;
	private static final int PACOTE_TAMANHO_LOW = 5;
	private static final int PACOTE_COMANDO = 6;
	private static final int PACOTE_DADOS = 7;
	private static final int PACOTE_CRC_HIGH = 8;
	private static final int PACOTE_CRC_LOW = 9;
	private static final int MEU_ENDERECO = 100;

	private static final int CMD_PING = 171;
	private static final int RESP_PING = 172;
	private static final int CMD_START_TIMER = 173;
	private static final int CMD_ENVIAR_DADOS = 174;
	private static final int CMD_STOP_TIMER = 175;
	private static final int CMD_ZERAR_PULSOS = 176;
	private static final int CMD_TENSAO = 177;
	private static final int CMD_TEMPERATURA = 178;
	private static final int CMD_TEMPERATURA_AGUA = 179;

	private Timer timer = new Timer();
	long StartTimer = 0;

	private int[] pacoteRecebido;
	private int qtdeDadosEsperados = 0;
	private int dadosRecebidos;
	private int estadoMaquina;
	public TextView tx_temp, tx_cont, time;
	public Button bt;
	public int[] vetor;
	public int ComandoRecebido, contagem;
	public long milisegundos;
	public double segundos;

	// Para atualizar o status do tratamento anterior no servidor
	private Socket socket;

	private double DistanciaVertical;

	private int i;
	private static final int CMD_SOCKET_ATUALIZAR_STATUS = 2;

	

	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.menuTensao) {
			montaPacote(177, 1, 1);
					
				
		} else if (item.getItemId() == R.id.menuTemperatura) {
			montaPacote(178, 1, 1);
						
				
	} else if (item.getItemId() == R.id.menuTemperatura_agua) {
		montaPacote(179, 1, 1);
		
	}else if (item.getItemId() == android.R.id.home) {
			finish();
		}

		return super.onOptionsItemSelected(item);
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_principal, menu);

		return true;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_comunicacao_bluetooth);

		handlerSerial = new Handler();
		pacoteRecebido = new int[1024];
		vetor = new int[100];
		vetor[0] = 4;
		vetor[1] = 5;

		Intent it = getIntent();
		DistanciaVertical = (double) it.getExtras().get("DistanciaVertical");
		
		device = (BluetoothDevice) it.getExtras().get("device");	
		service = ISPBluetoothService.getInstance();
		service.init(mHandler, MESSAGE_STATE_CHANGE, MESSAGE_READ,
				MESSAGE_WRITE, MESSAGE_DEVICE_NAME, MESSAGE_TOAST, DEVICE_NAME,
				TOAST);
		service.start();
		connectDevice(device, true);

		Button bt = (Button) findViewById(R.id.enviar);
		Button png = (Button) findViewById(R.id.ping);
		//Button tensao = (Button) findViewById(R.id.tensao);
		//Button temperatura = (Button) findViewById(R.id.temperatura);
		tx_temp = (TextView) findViewById(R.id.txvTemp);
		tx_cont = (TextView) findViewById(R.id.TxvCont);
		time = (TextView) findViewById(R.id.TxvTempo);	
		
		
		i =1;
		EntradaProfundiade();
		//passar valores por intent
		
		
		bt.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// montaPacote(181, 1, 2);
				tx_temp.setText(null);
				tx_cont.setText(null);
			}
		});
	

		png.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				try {
					// Verifica se o numero informado é maior que zero

					int tim = Integer.parseInt(time.getText().toString());
					vetor[0] = CMD_PING;
					montaPacote(CMD_PING, 1, 1);
				} catch (Exception e) {
					// TODO: handle exception
					Toast.makeText(getBaseContext(),
							"O tempo deve ser maior que 0", Toast.LENGTH_SHORT)
							.show();
				}

			}
		});

		ActionBar bar = getActionBar();
		bar.setDisplayHomeAsUpEnabled(true);
	}

	

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		// startActivity(new Intent(this, PrincipalActivity.class));
		overridePendingTransition(
				R.animator.animation_esquerda_para_direita_aparece,
				R.animator.animation_esquerda_para_direita_some);
		finish();
	}

	private void setStatus(String text) {
		ActionBar bar = getActionBar();
		bar.setSubtitle(text);
	}

	// M�todos necess�rios para o bluetooth
	private final Handler mHandler = new Handler() {

		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_STATE_CHANGE:
				switch (msg.arg1) {
				case ISPBluetoothService.STATE_CONNECTED:
					setStatus("Conectado com: " + mConnectedDeviceName);
					// montaPacote(181, 1,1);
					break;
				case ISPBluetoothService.STATE_CONNECTING:
					setStatus("Conectando...");
					break;
				case ISPBluetoothService.STATE_LISTEN:
					break;
				case ISPBluetoothService.STATE_NONE:
					setStatus("Sem conexão");
					break;
				}
				break;

			case MESSAGE_WRITE:
				byte[] writeBuf = (byte[]) msg.obj;
				String writeMessage = new String(writeBuf);
				break;
			case MESSAGE_READ:
				byte[] readBuf = (byte[]) msg.obj;
				int receiverByte = readBuf[0] & 0xff;
				// SetaTexto(msg.obj);
				// String teste =Integer.toString(receiverByte);

				decodificaPacote(receiverByte);
				break;
			case MESSAGE_DEVICE_NAME:
				mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
				break;
			case MESSAGE_TOAST:
				break;
			}
		}

	};

	/*
	 * O pacote ser� montado de acordo com os dados escolhidos na parte do
	 * servidor Socket + 9 posi��es fixas
	 */
	private void montaPacote(int comando, int endereco, int tamanho) {
		pacote = new byte[tamanho + 9];
		int parteAltaEndereco;
		int parteBaixaEndereco;
		int parteAltaMsg;
		int parteBaixaMsg;
		int parteAltaCrc;
		int parteBaixaCrc;
		int soma = 0;

		parteAltaEndereco = 65280 & endereco;
		parteAltaEndereco = parteAltaEndereco >> 8;
		parteBaixaEndereco = 255 & endereco;

		// Pegando o tamanho do array de dados para fazer os calculos
		parteAltaMsg = 65280 & (tamanho + 9);
		parteAltaMsg = parteAltaMsg >> 8;
		parteBaixaMsg = 255 & (tamanho + 9);

		pacote[0] = (byte) 170;
		soma += 170;
		pacote[1] = (byte) parteAltaEndereco;
		// pacote[1] = 0;
		soma += parteAltaEndereco;
		// soma += 0;
		pacote[2] = (byte) parteBaixaEndereco;
		// pacote[2] = 0;
		soma += parteBaixaEndereco;
		// soma += 0;
		pacote[3] = (byte) 129;
		soma += 129;
		pacote[4] = (byte) parteAltaMsg;
		soma += parteAltaMsg;
		pacote[5] = (byte) parteBaixaMsg;
		soma += parteBaixaMsg;
		pacote[6] = (byte) comando;
		soma += comando;
		for (int i = 0; i < tamanho; i++) {
			soma += vetor[i] & 0xff;
			pacote[7 + i] = (byte) vetor[i];
		}

		parteAltaCrc = 65280 & soma;
		parteAltaCrc = parteAltaCrc >> 8;
		parteBaixaCrc = 255 & soma;

		pacote[tamanho + PACOTE_DADOS] = (byte) parteAltaCrc;
		pacote[tamanho + PACOTE_DADOS + 1] = (byte) parteBaixaCrc;
		sendMessage(pacote);

	}

	private void decodificaPacote(int receiverByte) {
		switch (estadoMaquina) {
		case PACOTE_START:
			if (receiverByte == 170) {
				pacoteRecebido[PACOTE_START] = receiverByte;
			}
			estadoMaquina++;
			break;
		case PACOTE_ENDERECO_DESTINO_HIGH:
			pacoteRecebido[PACOTE_ENDERECO_DESTINO_HIGH] = receiverByte;
			estadoMaquina++;
			break;
		case PACOTE_ENDERECO_DESTINO_LOW:
			pacoteRecebido[PACOTE_ENDERECO_DESTINO_LOW] = receiverByte;
			estadoMaquina++;
			break;
		case PACOTE_CONFIG:
			pacoteRecebido[PACOTE_CONFIG] = receiverByte;
			estadoMaquina++;
			break;
		case PACOTE_TAMANHO_HIGH:
			pacoteRecebido[PACOTE_TAMANHO_HIGH] = receiverByte;
			estadoMaquina++;
			break;
		case PACOTE_TAMANHO_LOW:
			pacoteRecebido[PACOTE_TAMANHO_LOW] = receiverByte;
			estadoMaquina++;
			qtdeDadosEsperados = (pacoteRecebido[PACOTE_TAMANHO_HIGH] * 256)
					+ (pacoteRecebido[PACOTE_TAMANHO_LOW] - 9);
			break;
		case PACOTE_COMANDO:
			pacoteRecebido[PACOTE_COMANDO] = receiverByte;
			ComandoRecebido = receiverByte;
			if (qtdeDadosEsperados > 0) {
				estadoMaquina++;
			} else {
				estadoMaquina += 2;
			}
			break;
		case PACOTE_DADOS:
			pacoteRecebido[PACOTE_DADOS + dadosRecebidos] = receiverByte;
			dadosRecebidos++;
			if (dadosRecebidos == qtdeDadosEsperados) {
				estadoMaquina++;
			}
			break;
		case PACOTE_CRC_HIGH:
			pacoteRecebido[PACOTE_CRC_HIGH + dadosRecebidos - 1] = receiverByte;
			estadoMaquina++;
			break;
		case PACOTE_CRC_LOW:
			pacoteRecebido[PACOTE_CRC_LOW + dadosRecebidos - 1] = receiverByte;
			if (confereCrc() && confereEndereco()) {

				switch (ComandoRecebido) {
				case RESP_PING:

					// Comando zerar pulsos
					vetor[0] = CMD_ZERAR_PULSOS;
					montaPacote(CMD_ZERAR_PULSOS, 1, 1);
					break;

				case CMD_ZERAR_PULSOS:

					// Comando Iniciar timer
					vetor[0] = CMD_START_TIMER;
					vetor[1] = Integer.parseInt(time.getText().toString());

					montaPacote(CMD_START_TIMER, 1, 2);

					break;

				case CMD_START_TIMER:

					// Comando requisita dados
					vetor[0] = CMD_ENVIAR_DADOS;
					montaPacote(CMD_ENVIAR_DADOS, 1, 1);
					break;

				case CMD_ENVIAR_DADOS:

					if (pacoteRecebido[PACOTE_DADOS + 6] == 1) {
						// StartTimer = System.currentTimeMillis();
						// timer = new Timer();

						try {

							// Thread aguarda 100 milisegundos e chama novamente
							// o método monta pacote
							Thread.sleep(100);
							vetor[0] = CMD_ENVIAR_DADOS;
							montaPacote(CMD_ENVIAR_DADOS, 1, 1);

						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						// timer.schedule(new firstTask(), 0, 100);

						milisegundos = (pacoteRecebido[PACOTE_DADOS]
								+ (pacoteRecebido[PACOTE_DADOS + 1] * (256))
								+ (pacoteRecebido[PACOTE_DADOS + 2] * (65536)) + (pacoteRecebido[PACOTE_DADOS + 3] * (16777216)));

						contagem = (pacoteRecebido[PACOTE_DADOS + 4])
								+ (pacoteRecebido[PACOTE_DADOS + 5] * (256));

						NumberFormat df1 = NumberFormat.getNumberInstance();
						df1.setMaximumFractionDigits(2);
						segundos = (double) milisegundos / 1000;
						String segundos_formatado = df1.format(segundos);
						
						tx_temp.setText(String.valueOf(segundos_formatado));
						tx_cont.setText(String.valueOf(contagem));

					
					} else {

						// Comando para timer
						vetor[0] = CMD_STOP_TIMER;
						montaPacote(CMD_STOP_TIMER, 1, 1);

						// Cancela a execuçao das tarefa agendada
						// timer.cancel();
					}
					break;
				case CMD_TENSAO:
					// Comando solicita tensão
					// posicao do tamanho do pacote de dados e pacote de dados
					// +1, vezes 10/1024 - valor do setor
					Float tens = (float) ((pacoteRecebido[PACOTE_DADOS] +
							(pacoteRecebido[PACOTE_DADOS + 1] * 256)) * 0.009765625);
					NumberFormat df1 = NumberFormat.getNumberInstance();
					df1.setMaximumFractionDigits(1);
					String tensao_formatada = df1.format(tens);

					/*Toast.makeText(getBaseContext(),
							"Tensão da bateria : " + tensao_formatada + " V",
							Toast.LENGTH_SHORT).show();*/
					
					AlertDialog.Builder dialog = new AlertDialog.Builder(
							ComunicacaoBluetooth.this);
					dialog.setMessage("Tensão da bateria: " + tensao_formatada + " V");
					dialog.setNeutralButton("OK", null);
					dialog.setTitle("Bateria!");
					dialog.setIcon(R.drawable.ic_action_tensao);
					dialog.show();
					//montaPacote(CMD_ENVIAR_DADOS, 1, 1);
					break;

				case CMD_TEMPERATURA:
					// Comando solicita tensão
					// posicao do tamanho do pacote de dados e pacote de dados
					// +1, vezes 10/1024 - valor do setor
					Integer temp = (int) ((pacoteRecebido[PACOTE_DADOS]));

					/*Toast.makeText(getBaseContext(), "Temperatura: " + temp + "°",
							Toast.LENGTH_SHORT).show();
					//montaPacote(CMD_ENVIAR_DADOS, 1, 1);*/
					AlertDialog.Builder dialog2 = new AlertDialog.Builder(
							ComunicacaoBluetooth.this);
					dialog2.setMessage("Temperatura ambiente: " + temp + "°");
					dialog2.setNeutralButton("OK", null);
					dialog2.setTitle("Temperatura!");
					dialog2.setIcon(R.drawable.ic_action_temp_ambiente);
					dialog2.show();
					break;
					
				case CMD_TEMPERATURA_AGUA:
					// Comando solicita tensão
					// posicao do tamanho do pacote de dados e pacote de dados
					// +1, vezes 10/1024 - valor do setor
					Integer temp_agua = (int) ((pacoteRecebido[PACOTE_DADOS]));

					/*Toast.makeText(getBaseContext(), "Temperatura da água: " + temp_agua + "°",
							Toast.LENGTH_SHORT).show();
					*/
					AlertDialog.Builder dialog3 = new AlertDialog.Builder(
							ComunicacaoBluetooth.this);
					dialog3.setMessage("Temperatura da água: " + temp_agua + "°");
					dialog3.setNeutralButton("OK", null);
					dialog3.setTitle("Temperatura!");
					dialog3.setIcon(R.drawable.ic_action_temperatura_agua);
					dialog3.show();
					//montaPacote(CMD_ENVIAR_DADOS, 1, 1);
					break;	
					
			
				}


				limparVariaveis();
				// aqui vai ser usado um handler, e no m�todo Runnable
				// vai ficar a l�gica para habilitar novamente o bot�o
				handlerSerial.post(this);
			}
			break;
		default:
			break;
		}
	}

	class firstTask extends TimerTask {

		@Override
		public void run() {
			vetor[0] = CMD_ENVIAR_DADOS;
			montaPacote(CMD_ENVIAR_DADOS, 1, 1);
		}
	};

	private boolean confereEndereco() {
		int endereco = (pacoteRecebido[PACOTE_ENDERECO_DESTINO_HIGH] * 256)
				+ pacoteRecebido[PACOTE_ENDERECO_DESTINO_LOW];
		if (endereco == MEU_ENDERECO) {
			return true;
		}
		return false;
	}

	private boolean confereCrc() {
		int soma = 0;
		int crc = 0;
		crc = ((pacoteRecebido[PACOTE_CRC_HIGH + qtdeDadosEsperados - 1]) * 256)
				+ (pacoteRecebido[PACOTE_CRC_LOW + qtdeDadosEsperados - 1]);
		for (int i = 0; i < qtdeDadosEsperados + 7; i++) {
			soma += pacoteRecebido[i];
		}
		// soma = soma / 2;
		if (soma == crc) {
			return true;
		}
		return false;
	}

	private void limparVariaveis() {
		dadosRecebidos = 0;
		estadoMaquina = PACOTE_START;
		pacoteRecebido = new int[1024];
	}

	private void connectDevice(BluetoothDevice device, boolean secure) {
		service.connect(device, secure);
	}

	private void sendMessage(byte[] bytes) {
		if (service.getState() != com.isp.bluetoothservicelib.services.ISPBluetoothService.STATE_CONNECTED) {
			Toast.makeText(this, "Sem conexao", Toast.LENGTH_SHORT).show();
			return;
		}
		service.write(bytes);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		service.stop();
	}

	@Override
	public void run() {
		int comando = pacoteRecebido[PACOTE_COMANDO];
		if (comando == 181) {
			Toast.makeText(this, "Ping recebido", Toast.LENGTH_SHORT).show();
			layoutEquipamento.setEnabled(true);
			// montaPacote(184, 1, 1);
			limparVariaveis();
		} else if (comando == 182) {
			Toast.makeText(this, "Servidor parou", Toast.LENGTH_SHORT).show();
			service.stop();
			limparVariaveis();
		} else if (comando == 183) {
			Toast.makeText(this, "Equipamento ocupado", Toast.LENGTH_SHORT)
					.show();
			service.stop();
			limparVariaveis();
			finish();
		} else if (comando == 184) {
			int idUltimaTerapia = pacoteRecebido[PACOTE_DADOS];
			int status = pacoteRecebido[PACOTE_DADOS + 1];
			layoutEquipamento.setEnabled(true);
			Toast.makeText(this,
					"Status do ultimo tratamento recebido: " + status,
					Toast.LENGTH_SHORT).show();
			Toast.makeText(this, "Id da ultima terapia: " + idUltimaTerapia,
					Toast.LENGTH_SHORT).show();

			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(this);
			String ip = prefs.getString("ip_server", "192.168.10.177");
			InetSocketAddress endereco = new InetSocketAddress(ip, 7777);
			try {
				socket = new Socket();
				socket.connect(endereco, 10000);
				DataOutputStream output = new DataOutputStream(
						socket.getOutputStream());
				output.writeInt(CMD_SOCKET_ATUALIZAR_STATUS);
				// Mandando para o servidor o id do ultima terapia
				// e o status do equipamento
				output.writeInt(pacoteRecebido[PACOTE_DADOS]);
				output.writeInt(status);

				// O id e o status do tratamento selecionado ser� enviado para o
				// servidor

				socket.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	private void EntradaProfundiade() {
		// TODO Auto-generated method stub
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		
		builder.setTitle("Profundidade ");
		builder.setMessage("Informe a profundidade da vertical " + /*DistanciaVertical*/ i);
		builder.setIcon(R.drawable.ic_launcher);
	
	final EditText profundidade = new EditText(this);
	profundidade.setSingleLine();
	profundidade.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);	
	profundidade.setText("");	
	
	
	builder.setView(profundidade);

	builder.setPositiveButton("OK",
			new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					
					dialog.dismiss();
					
				}

			});

	builder.setNegativeButton("CANCELAR",
			new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {

					dialog.dismiss();
				}
				
			});
	i++;
	
	AlertDialog alert = builder.create();
	alert.show();
	
	
	
	}

}
