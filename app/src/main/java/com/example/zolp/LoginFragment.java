package com.example.zolp;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;


public class LoginFragment extends Fragment {
    MainActivity main;
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        main = (MainActivity)getActivity();
        BackPressHandler handler = new BackPressHandler(main);
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                handler.onBackPressed();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_login, container, false);
        EditText loginId = rootView.findViewById(R.id.login_id);
        EditText loginPw = rootView.findViewById(R.id.login_pw);
        Button joinBtn = rootView.findViewById(R.id.join_btn);
        Button loginBtn = rootView.findViewById(R.id.login_btn);

        joinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JoinFragment joinFragment = new JoinFragment();
                main.getSupportFragmentManager().beginTransaction().replace(R.id.fragment_content_main, joinFragment).addToBackStack(null).commit();
            }
        });
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginBtnClick(loginId.getText().toString(), loginPw.getText().toString());
            }
        });

        return rootView;
    }

    private void loginBtnClick(String id, String pw){
        if(id.equals("")){
            Toast.makeText(getContext(), "아이디를 입력해주세요!", Toast.LENGTH_SHORT).show();
        }
        else if(pw.equals("")){
            Toast.makeText(getContext(), "비밀번호를 입력해주세요!", Toast.LENGTH_SHORT).show();
        }
        else{
            loginProcess(id, pw);
        }
    }

    private void loginProcess(String id, String pw){
        main.mAuth.signInWithEmailAndPassword(id, pw).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    main.getSupportFragmentManager().popBackStack();
                    main.getSupportFragmentManager().beginTransaction().replace(R.id.fragment_content_main, new MainFragment()).commit();
                }
                else{
                    Toast.makeText(getContext(), "로그인 실패", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onDetach() {
        super.onDetach();
        main = null;
    }
}