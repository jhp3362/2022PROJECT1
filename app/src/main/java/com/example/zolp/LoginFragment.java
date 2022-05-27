package com.example.zolp;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;


public class LoginFragment extends Fragment {
    MainActivity main;
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        main = (MainActivity)getActivity();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        if(main.mAuth.getCurrentUser()!=null){
            main.getSupportFragmentManager().beginTransaction().replace(R.id.fragment_content_main, new MainFragment()).commit();
        }
        main.userView.setVisibility(View.INVISIBLE);
        main.toolbar.setTitle("로그인");
        View rootView = inflater.inflate(R.layout.fragment_login, container, false);
        EditText loginId = rootView.findViewById(R.id.login_Id);
        EditText loginPw = rootView.findViewById(R.id.login_Pw);
        Button joinBtn = rootView.findViewById(R.id.join_Btn);
        Button loginBtn = rootView.findViewById(R.id.login_Btn);



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