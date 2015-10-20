package com.dados;

import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.List;

import com.bluetooth.ComunicacaoBluetooth;
import com.bluetooth.ListaDispositivosActivity;
import com.bluetooth.MainActivity;
import com.example.bluetooth.R;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class DadosIniciais extends Activity {

	private Spinner spinner;
	private List<String> valores = new ArrayList<String>();
	String valor_selecionado;
	private EditText DistanciaVerticais;
	private CheckBox alterarDistanciaVerticais;
	protected String qntdvertical;
	private EditText larguraRio;
	
	private BluetoothDevice device;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dados);

		ActionBar bar = getActionBar();
		bar.setTitle("Selecione a distancia do rio");
		bar.setDisplayHomeAsUpEnabled(true);
		
		Intent it = getIntent();			
		device = (BluetoothDevice) it.getExtras().get("device");	

		DistanciaVerticais = (EditText) findViewById(R.id.EditDistanciaVerticais);
		// spinner = (Spinner) findViewById(R.id.spinner1);
		larguraRio = (EditText) findViewById(R.id.EditLarguraRio);
		alterarDistanciaVerticais = (CheckBox) findViewById(R.id.checkBox1);
		Button avancar = (Button) findViewById(R.id.Largura_avancar);

		larguraRio.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				try {
					AtualizaValores();
				} catch (Exception e) {

				}
			}
		});

		alterarDistanciaVerticais.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				if (alterarDistanciaVerticais.isChecked() == true) {
					DistanciaVerticais.setFocusable(true);
					DistanciaVerticais.setFocusableInTouchMode(true);

				} else {
					DistanciaVerticais.setFocusable(false);
				}
			}
		});

		avancar.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
			
				if (DistanciaVerticais.getText().toString().length() > 0) {
					
					int LarguraRio = Integer.parseInt(larguraRio.getText()
							.toString());
					double QuantidadedeVerticais = Double
							.parseDouble(DistanciaVerticais.getText().toString());
					QuantidadedeVerticais = LarguraRio / QuantidadedeVerticais;

					
					Intent intent = new Intent(DadosIniciais.this,
							ComunicacaoBluetooth.class);
					intent.putExtra("DistanciaVertical", QuantidadedeVerticais);
					intent.putExtra("device", device);
					
					
					overridePendingTransition(
							R.animator.animation_direita_para_esquerda_aparece,
							R.animator.animation_direita_para_esquerda_some);
					startActivity(intent);
				}else{				
					larguraRio.setError("Selecione a largura do rio");
					larguraRio.requestFocus();
				}
			}
		});
	}

	protected void AtualizaValores() {
		// TODO Auto-generated method stub

		System.out
				.println("largura do rio: " + larguraRio.getText().toString());

		if (larguraRio.getText().toString().length() > 0) {

			if (Integer.parseInt(larguraRio.getText().toString()) <= 3) {
				DistanciaVerticais.setText("0.3 ");

			} else if (Integer.parseInt(larguraRio.getText().toString()) > 3
					&& Integer.parseInt(larguraRio.getText().toString()) <= 6) {
				DistanciaVerticais.setText("0.5 ");

			} else if (Integer.parseInt(larguraRio.getText().toString()) > 6
					&& Integer.parseInt(larguraRio.getText().toString()) <= 15) {
				DistanciaVerticais.setText("1.0 ");

			} else if (Integer.parseInt(larguraRio.getText().toString()) > 15
					&& Integer.parseInt(larguraRio.getText().toString()) <= 30) {
				DistanciaVerticais.setText("2.0 ");

			} else if (Integer.parseInt(larguraRio.getText().toString()) > 30
					&& Integer.parseInt(larguraRio.getText().toString()) <= 50) {
				DistanciaVerticais.setText("3.0 ");

			} else if (Integer.parseInt(larguraRio.getText().toString()) > 50
					&& Integer.parseInt(larguraRio.getText().toString()) <= 80) {
				DistanciaVerticais.setText("4.0 ");

			} else if (Integer.parseInt(larguraRio.getText().toString()) > 80
					&& Integer.parseInt(larguraRio.getText().toString()) <= 150) {
				DistanciaVerticais.setText("6.0 ");

			} else if (Integer.parseInt(larguraRio.getText().toString()) > 150
					&& Integer.parseInt(larguraRio.getText().toString()) <= 250) {
				DistanciaVerticais.setText("8.0 ");

			} else if (Integer.parseInt(larguraRio.getText().toString()) > 250) {
				DistanciaVerticais.setText("12.0 ");
			}
		}

	}

	public void CarregaSpinner() throws Exception {
		// TODO Auto-generated method stub

		ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_dropdown_item, valores);
		ArrayAdapter<String> spinnerArrayAdapter = arrayAdapter;
		spinnerArrayAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(spinnerArrayAdapter);

		spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View v,
					int posicao, long id) {

				switch (posicao) {
				case 0:
					DistanciaVerticais.setText(" ");
					break;
				case 1:
					DistanciaVerticais.setText("0,3m ");
					break;
				case 2:
					DistanciaVerticais.setText("0,5m ");
					break;
				case 3:
					DistanciaVerticais.setText("1,0m ");
					break;
				case 4:
					DistanciaVerticais.setText("2,0m ");
					break;
				case 5:
					DistanciaVerticais.setText("3,0m ");
					break;
				case 6:
					DistanciaVerticais.setText("4,0m ");
					break;
				case 7:
					DistanciaVerticais.setText("6,0m ");
					break;
				case 8:
					DistanciaVerticais.setText("8,0m ");
					break;
				case 9:
					DistanciaVerticais.setText("12,0m ");
					break;

				default:

					Toast.makeText(getBaseContext(), "Erro", Toast.LENGTH_SHORT)
							.show();
					break;
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
			}
		});
	}

}
