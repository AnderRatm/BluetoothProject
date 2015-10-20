package com.isp.bluetoothservicelib;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;

//Essa classe pode ser usada a fins de teste
//Outras classes podem ser criadas no futuro
public class PrincipalActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_principal);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.principal, menu);
		return true;
	}

}
