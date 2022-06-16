package com.example.zolp;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;


public class JoinFragment extends Fragment {

    private EditText joinPw;
    private EditText joinPwCheck;
    private TextView pwChecker;
    private EditText joinName;
    private Boolean bChecker = false;
    MainActivity main;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        main = (MainActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_join, container, false);

        main.toolbar.setTitle("회원가입");

        Button joinBtn = rootView.findViewById(R.id.join_Btn);
        EditText joinId = rootView.findViewById(R.id.join_Id);
        joinName = rootView.findViewById(R.id.join_Name);
        joinPw = rootView.findViewById(R.id.join_Pw);
        joinPwCheck = rootView.findViewById(R.id.join_Pwcheck);
        pwChecker = rootView.findViewById(R.id.pwChecker);

        //password check
        joinPw.addTextChangedListener(textWatcher);
        joinPwCheck.addTextChangedListener(textWatcher);


        joinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                joinBtnClick(joinId.getText().toString(), joinPw.getText().toString(), joinName.getText().toString());
            }
        });
        return rootView;
    }

    private final TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if(!joinPwCheck.getText().toString().equals("")) {
                if (joinPwCheck.getText().toString().equals(joinPw.getText().toString())) {
                    pwChecker.setText("일치합니다!");
                    pwChecker.setTextColor(Color.parseColor("#4CAF50"));
                    bChecker = true;
                } else {
                    pwChecker.setText("일치하지 않습니다!");
                    pwChecker.setTextColor(Color.parseColor("#FF0000"));
                    bChecker = false;
                }
            }
            else{
                pwChecker.setText("");
                bChecker = false;
            }
        }
    };

    private void joinBtnClick(String id, String pw, String name){
        if(id.equals("")){
            Toast.makeText(getContext(), "아이디를 입력해주세요!", Toast.LENGTH_SHORT).show();
        }
        else if(name.equals("")){
            Toast.makeText(getContext(), "이름을 입력해주세요!", Toast.LENGTH_SHORT).show();
        }
        else if(pw.equals("")){
            Toast.makeText(getContext(), "비밀번호를 입력해주세요!", Toast.LENGTH_SHORT).show();
        }
        else if(!bChecker){
            Toast.makeText(getContext(), "비밀번호가 일치하지 않습니다!", Toast.LENGTH_SHORT).show();
        }
        else if(pw.length()<6){
            Toast.makeText(getContext(), "비밀번호는 6자리 이상이어야 합니다!", Toast.LENGTH_SHORT).show();
        }
        else{
            joinProcess(id, pw, name);
        }
    }

    private void joinProcess(String id, String pw, String name){
        main.mAuth.createUserWithEmailAndPassword(id, pw).addOnCompleteListener(main, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Toast.makeText(getContext(),"가입 성공",Toast.LENGTH_SHORT).show();
                    UserProfileChangeRequest req = new UserProfileChangeRequest.Builder().setDisplayName(name).build();
                    main.mAuth.getCurrentUser().updateProfile(req);
                    main.mAuth.signOut();
                    main.getSupportFragmentManager().beginTransaction().replace(R.id.fragment_content_main, new LoginFragment()).commit();
                }
                else{
                    Toast.makeText(getContext(),"가입 실패" ,Toast.LENGTH_SHORT).show();
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