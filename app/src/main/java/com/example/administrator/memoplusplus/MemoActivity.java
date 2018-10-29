package com.example.administrator.memoplusplus;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MemoActivity extends AppCompatActivity {
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private static FirebaseDatabase mFirebaseDatabase;

    private EditText edContent, titleCon;

    private String selectedMemoKey;

    private Intent sendIntent;

    static {
        mFirebaseDatabase = FirebaseDatabase.getInstance();
    }

    String title, text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memo);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        edContent = (EditText) findViewById(R.id.content);
        titleCon = (EditText) findViewById(R.id.titleCon);

        final Intent getIntent = getIntent();
        title = getIntent.getStringExtra("title");
        text = getIntent.getStringExtra("text");
        selectedMemoKey = getIntent.getStringExtra("key");

        titleCon.setText(title);
        edContent.setText(text);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        if (mFirebaseUser == null) {//유저값이 없을시(로그인이 되지않았을시) AuthActivity로 이동
            startActivity(new Intent(MemoActivity.this, AuthActivity.class));
            finish();
            return;
        }

        FloatingActionButton saveButton = (FloatingActionButton) findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedMemoKey == null) {//selectedMemoKey가 있는값 = 존재하지 않는 메모이므로 새로저장
                    saveMemo();
                } else {//selectedMemoKey가 있는값 = 이미 있는메모이므로 수정
                    updateMemo();
                }

            }
        });
        FloatingActionButton initButton = (FloatingActionButton) findViewById(R.id.initButton);
        initButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initMemoValue();
            }
        });
        FloatingActionButton deleteButton = (FloatingActionButton) findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteMemo();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_memo, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.menuShare) {
            shareMemo();
            return true;
        }
        if (id == R.id.menuCopy) {
            copyMemo();
            return true;
        }
        if (id == android.R.id.home) {
            if(selectedMemoKey==null){
                if(edContent.getText().toString().equals("")){
                    finish();
                    return true;
                }else{
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                    alertDialogBuilder.setTitle("메모를 저장하시겠습니까?")
                            .setCancelable(true)
                            .setPositiveButton("예", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    saveMemo();
                                }
                            }).setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                            return;
                        }
                    });
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                }
            }else{
                if(title.equals(titleCon.getText().toString())&&text.equals(edContent.getText().toString())){
                    finish();
                    return true;
                }else{
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                    alertDialogBuilder.setTitle("메모를 변경하시겠습니까?")
                            .setCancelable(true)
                            .setPositiveButton("예", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    updateMemo();
                                }
                            }).setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finish();
                            return;
                        }
                    });
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                }
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveMemo() {//메모 저장
        String strTitle = titleCon.getText().toString();
        String text = edContent.getText().toString();
        if (text.isEmpty()) {
            return;
        }
        if (strTitle.isEmpty()) {
            strTitle = "제목이 없는 메모";
        }
        Date date = new Date();
        String sdfDate = new SimpleDateFormat("yyyy년 MM월 dd일 hh:mm:ss a").format(date);
        MemoData memo = new MemoData();
        memo.setTitle(strTitle);
        memo.setTxt(text);
        memo.setStrDate(sdfDate);
        mFirebaseDatabase
                .getReference(mFirebaseUser
                        .getUid()).child("/memos").push().setValue(memo)
                .addOnSuccessListener(MemoActivity.this, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        sendIntent = new Intent(MemoActivity.this, MainActivity.class);
                        sendIntent.putExtra("save", "메모 저장 완료");
                        startActivity(sendIntent);
                        sendIntent = null;
                        finish();
                        return;
                    }
                });
    }

    private void updateMemo() {//메모 수정
        String strTitle = titleCon.getText().toString();
        String text = edContent.getText().toString();
        if (text.isEmpty()) {
            return;
        }
        if (strTitle.isEmpty()) {
            strTitle = "제목이 없는 메모";
        }
        MemoData memo = new MemoData();
        memo.setTxt(text);
        memo.setTitle(strTitle);
        Date date = new Date();
        String sdfDate = new SimpleDateFormat("yyyy년 MM월 dd일 hh:mm:ss a").format(date);
        memo.setStrDate(sdfDate);
        mFirebaseDatabase
                .getReference(mFirebaseUser.getUid()).child("/memos").child(selectedMemoKey)
                .setValue(memo).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                sendIntent = new Intent(MemoActivity.this, MainActivity.class);
                sendIntent.putExtra("edit", "메모 수정 완료");
                startActivity(sendIntent);
                sendIntent = null;
                finish();
                return;
            }
        });
    }

    private void initMemoValue() {//액티비티 내의 edittext비우기
        titleCon.setText("");
        edContent.setText("");
    }

    private void deleteMemo() {//메모 삭제
        if (selectedMemoKey == null) {//메모가 저장이안되서 없다면 클릭해도 삭제 AlertDialog를 띄우지않음
            return;
        }
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("메모를 삭제하시겠습니까?")
                .setCancelable(false)
                .setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mFirebaseDatabase.getReference(mFirebaseUser.getUid()).child("/memos").child(selectedMemoKey)
                                .removeValue(new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                        sendIntent = new Intent(MemoActivity.this, MainActivity.class);
                                        sendIntent.putExtra("delete", "메모 삭제 완료");
                                        startActivity(sendIntent);
                                        sendIntent = null;
                                        finish();
                                        return;
                                    }
                                });
                    }
                }).setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void copyMemo() {//ClipboardManager사용 메모 내용쪽 edittext에 있는 내용을 클립보드에 복사
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("label", edContent.getText().toString());
        clipboardManager.setPrimaryClip(clipData);
        Toast.makeText(this, "메모내용이 클립보드에 복사되었습니다.", Toast.LENGTH_SHORT).show();
    }

    private void shareMemo() {//intent사용
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, titleCon.getText().toString());
        shareIntent.putExtra(Intent.EXTRA_TEXT, edContent.getText().toString());
        startActivity(Intent.createChooser(shareIntent, "공유"));
    }
}
