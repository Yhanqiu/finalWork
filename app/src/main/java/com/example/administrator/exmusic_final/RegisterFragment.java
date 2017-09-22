package com.example.administrator.exmusic_final;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by Administrator on 2017/9/21.
 */

public class RegisterFragment extends Fragment {

    Button loginBt;
    Button button;
    EditText nameEdit;
    EditText pwEdit;
    LinearLayout loginLinear;
    LinearLayout userInfo;
    FrameLayout root;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.login_fragment, container, true);
        loginBt = (Button) view.findViewById(R.id.loginButton);
        nameEdit = (EditText) view.findViewById(R.id.idEdit);
        pwEdit = (EditText) view.findViewById(R.id.PwEdit);
        loginLinear = (LinearLayout) view.findViewById(R.id.loginLinear);
        userInfo = (LinearLayout) view.findViewById(R.id.user_info);
        root = (FrameLayout) view.findViewById(R.id.login_fragment);
        button = (Button)view.findViewById(R.id.button);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        loginBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("testtest", "hhh");
//                root.bringChildToFront(userInfo);
                loginLinear.setVisibility(View.INVISIBLE);

                loginBt.setText("退出");
            }
        });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("btbtbt","btbt");
                loginLinear.setVisibility(View.VISIBLE);
            }
        });
    }
}
