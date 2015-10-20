package com.adapter;

import java.util.List;
import com.example.bluetooth.R;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ListaDispositivosAdapter extends BaseAdapter {

	private static final String TAG = null;
	private Context context;
	private List<BluetoothDevice> list;
	
	public ListaDispositivosAdapter(Context context, List<BluetoothDevice> list){
		this.context = context;
		this.list = list;
	}
	
	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.layout_adapter_lista_dispositivos, null);
		TextView textViewNomeDispositivo = (TextView) view.findViewById(R.id.textViewAdapterNomeDispositivo);
		TextView textViewMacDispositivo = (TextView) view.findViewById(R.id.textViewAdapterMacDispositivo);
		
//		LayoutAnimationController controllerLeft = 
//				AnimationUtils.loadLayoutAnimation(context, R.animator.layout_animation_left);
//		LayoutAnimationController controllerRight = 
//				AnimationUtils.loadLayoutAnimation(context, R.animator.layout_animation_right);
//		
//		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
//		boolean animacao = prefs.getBoolean("animacao", true);
//		if(animacao){
//			if(position % 2 == 0){
//				view.setAnimation(controllerLeft.getAnimation());
//			} else {
//				view.setAnimation(controllerRight.getAnimation());
//			}
//		}
		
		textViewNomeDispositivo.setTextColor(Color.rgb(51, 181, 229));
		
		BluetoothDevice device = list.get(position);
		
		textViewNomeDispositivo.setText(device.getName());
		textViewMacDispositivo.setText(device.getAddress());
		
		/*try {
		Typeface tf = Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Light.ttf");
		textViewNomeDispositivo.setTypeface(tf);
		textViewMacDispositivo.setTypeface(tf);
		}catch (Exception e) {
			Log.e(TAG, "Could not get typeface '" 
                    + "' because " + e.getMessage());
            return null;
            	
        }*/
		return view;
	}

}
