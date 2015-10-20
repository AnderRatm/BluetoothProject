package com.bluetooth;



import com.dados.DadosIniciais;
import com.example.bluetooth.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;


public class MainActivity extends Activity{
		
		@Override
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.activity_main);
			

			Button bt1 = (Button)findViewById(R.id.Largura_avancar);
			
			bt1.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					Intent intent = new Intent(MainActivity.this, ListaDispositivosActivity.class);
					
					//intent.putExtra("paciente", paciente);
					startActivity(intent);
					overridePendingTransition(R.animator.animation_direita_para_esquerda_aparece, 
							R.animator.animation_direita_para_esquerda_some);
					
					
				}

			
			});
			
		}
		
		
	}

	
	


