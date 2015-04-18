package com.techila.travelfeedback;

import com.techila.travelfeedback.fragment.LoginFragment;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public class LoginActivity extends FragmentActivity {

	LoginFragment loginFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		if (savedInstanceState == null) {

			loginFragment = new LoginFragment();

			getSupportFragmentManager().beginTransaction()
					.add(android.R.id.content, loginFragment).commit();

		} else {
			loginFragment = (LoginFragment) getSupportFragmentManager()
					.findFragmentById(android.R.id.content);
		}

	}

}
