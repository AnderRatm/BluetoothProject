package com.bluetooth;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;







import java.util.UUID;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.Toast;

import com.adapter.ListaDispositivosAdapter;
import com.dados.DadosIniciais;
import com.example.bluetooth.R;


@TargetApi(Build.VERSION_CODES.HONEYCOMB)
@SuppressLint("NewApi")
public class ListaDispositivosActivity extends Activity {

	private GridView gridViewDispositivos;
	private ListaDispositivosAdapter adapter;
	private List<BluetoothDevice> listDevices;

	public static int REQUEST_ENABLE_BT = 3;
	private BluetoothAdapter mBluetoothAdapter;


	private BluetoothDevice device;

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_lista_dispositivos);

		ActionBar bar = getActionBar();
		bar.setTitle("Selecione um dispositivo");
		bar.setDisplayHomeAsUpEnabled(true);


		gridViewDispositivos = (GridView) findViewById(R.id.gridViewDispositivos);

		listDevices = new ArrayList<BluetoothDevice>();
		adapter = new ListaDispositivosAdapter(this, listDevices);
		gridViewDispositivos.setAdapter(adapter);
		adapter.notifyDataSetChanged();

		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if(mBluetoothAdapter != null) {

			if (!mBluetoothAdapter.isEnabled()) {
				Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableBluetooth, REQUEST_ENABLE_BT);
				//mBluetoothAdapter.startDiscovery();
				this.onResume();
			}
			Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();
			for (BluetoothDevice d : devices) {
				listDevices.add(d);
				adapter.notifyDataSetChanged();
			}
			mBluetoothAdapter.startDiscovery();
			IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
			registerReceiver(mReceiver, filter);

			gridViewDispositivos.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> av, View v, int position, long id) {
					device = listDevices.get(position);
					BluetoothDevice d = listDevices.get(position);
					//Intent intent = new Intent(ListaDispositivosActivity.this, ComunicacaoBluetooth.class);
					Intent intent = new Intent(ListaDispositivosActivity.this, DadosIniciais.class);
					//intent.putExtra("paciente", paciente);
					intent.putExtra("device", device);
					startActivity(intent);
					//finish();
					//conecta_thread();

					overridePendingTransition(R.animator.animation_direita_para_esquerda_aparece,
							R.animator.animation_direita_para_esquerda_some);
				}


			});
		}
		else{
			Toast.makeText(this, "Adaptador bluetooth não existe", Toast.LENGTH_SHORT).show();
		}

	}


	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if (mBluetoothAdapter != null) {
			Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();
			for (BluetoothDevice d : devices) {
				if (listDevices == null) {
					listDevices.add(d);
					adapter.notifyDataSetChanged();
				}
			}
			mBluetoothAdapter.startDiscovery();
			IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
			registerReceiver(mReceiver, filter);

			gridViewDispositivos.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> av, View v, int position, long id) {
					device = listDevices.get(position);
					BluetoothDevice d = listDevices.get(position);
					Intent intent = new Intent(ListaDispositivosActivity.this, DadosIniciais.class);
					//Intent intent = new Intent(ListaDispositivosActivity.this, ComunicacaoBluetooth.class);
					//intent.putExtra("paciente", paciente);
					intent.putExtra("device", device);
					startActivity(intent);
					//finish();
					//conecta_thread();

					overridePendingTransition(R.animator.animation_direita_para_esquerda_aparece,
							R.animator.animation_direita_para_esquerda_some);
				}
			});
		}

	}

	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		if (itemId == android.R.id.home) {
			onBackPressed();
		} else if (itemId == R.id.menuAtualizar) {
			if(!listDevices.isEmpty()){
				listDevices.clear();
				adapter.notifyDataSetChanged();
			}
			if (mBluetoothAdapter != null) {
				mBluetoothAdapter.startDiscovery();
				IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
				registerReceiver(mReceiver, filter);
			}
		} else {
		}
		return super.onOptionsItemSelected(item);
	}

	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

		public void onReceive(android.content.Context context, Intent intent) {
			String action = intent.getAction();
			if(BluetoothDevice.ACTION_FOUND.equals(action)){
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				if(!listDevices.contains(device)){
					listDevices.add(device);
					adapter.notifyDataSetChanged();
				}
			}
			if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
				mBluetoothAdapter.cancelDiscovery();
			}
		}

	};

	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.animator.animation_esquerda_para_direita_aparece,
				R.animator.animation_esquerda_para_direita_some);
		if (mBluetoothAdapter != null) {
			mBluetoothAdapter.cancelDiscovery();
			unregisterReceiver(mReceiver);
		}
		finish();
	}

	@SuppressLint("NewApi")
	private void setStatus(String text){
		ActionBar bar = getActionBar();
		bar.setSubtitle(text);
	}

}
