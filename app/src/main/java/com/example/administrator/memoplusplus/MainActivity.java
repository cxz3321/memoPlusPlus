package com.example.administrator.memoplusplus;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private static FirebaseDatabase mFirebaseDatabase;

    static {
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseDatabase.setPersistenceEnabled(true);
    }

    private ListView m_oListView = null;
    private ArrayList<ItemData> oData = null;
    private ArrayList<ItemData> oSearchData = null;
    private ListAdapter oAdapter;
    private Intent getIntent;
    private MenuItem searchItem;
    private SearchView searchView;

    private static String save, edit, cancel, delete;
    private static int i;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        i = 0;

        m_oListView = (ListView) findViewById(R.id.listView);
        oData = new ArrayList<>();
        oSearchData = new ArrayList<>();
        oSearchData.addAll(oData);

        if (mFirebaseUser == null) {//mFirebaseUser값이 없을시 인증액티비티로 넘어감
            startActivity(new Intent(MainActivity.this, AuthActivity.class));
            finish();
            return;
        }
        m_oListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                Intent i = new Intent(MainActivity.this, MemoActivity.class);
                i.putExtra("title", oData.get(position).strTitle);
                i.putExtra("text", oData.get(position).getTxt());
                i.putExtra("key", oData.get(position).getKey());
                startActivityForResult(i, 103);
            }
        });

        //메모추가범위
        com.github.clans.fab.FloatingActionButton fab = (com.github.clans.fab.FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent memoIntent = new Intent(MainActivity.this, MemoActivity.class);
                startActivityForResult(memoIntent, 100);
            }
        });

    }


    //설정창범위
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        searchItem = menu.findItem(R.id.action_search);
        searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint("검색");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Toast.makeText(MainActivity.this, "onQueryTextSubmit", Toast.LENGTH_SHORT).show();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                oSearchData.clear();
                if (newText.length() == 0) {
                    oAdapter = new ListAdapter(oData);
                    m_oListView.setAdapter(oAdapter);
                    oAdapter.notifyDataSetChanged();
                    return true;
                } else {
                    for (int i = 0; i < oData.size(); i++) {
                        if (oData.get(i).strTitle.toLowerCase().contains(newText)) {
                            oSearchData.add(oData.get(i));
                        }
                    }
                    oAdapter = new ListAdapter(oSearchData);
                    m_oListView.setAdapter(oAdapter);
                    oAdapter.notifyDataSetChanged();
                }
                return true;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.setting) {
            Intent settingIntent = new Intent(MainActivity.this, SettingActivity.class);
            startActivityForResult(settingIntent, 100);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void addList(String title, String date, String txt, String key) {
        ItemData oItem = new ItemData();
        oItem.strTitle = title;
        oItem.strDate = date;
        oItem.setTxt(txt);
        oItem.setKey(key);
        oData.add(oItem);
    }

    @Override
    protected void onResume() {
        super.onResume();
        oData.clear();
        displayMemo();
        getIntent = getIntent();
        save = getIntent.getStringExtra("save");
        edit = getIntent.getStringExtra("edit");
        cancel = getIntent.getStringExtra("cancel");
        delete = getIntent.getStringExtra("delete");
        if (i == 1) {
            i = 0;
            return;
        } else {
            if (cancel == null && save != null) {
                Toast.makeText(this, save, Toast.LENGTH_SHORT).show();
                return;
            } else if (cancel == null && edit != null) {
                Toast.makeText(this, edit, Toast.LENGTH_SHORT).show();
                return;
            } else if (cancel == null && delete != null) {
                Toast.makeText(this, delete, Toast.LENGTH_SHORT).show();
                return;
            }
        }
    }

    @Override
    protected void onRestart() {
        m_oListView.setAdapter(null);
        if (oAdapter == null) {

        } else {
            oAdapter.notifyDataSetChanged();
        }
        super.onRestart();
        if (cancel == null && save != null) {
            i = 1;
            return;
        }
        if (cancel == null && edit != null) {
            i = 1;
            return;
        }
        if (cancel == null && delete != null) {
            i = 1;
            return;
        }
    }

    private void displayMemo() {
        mFirebaseDatabase.getReference(mFirebaseUser.getUid().toString()).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    MemoData memos = ds.getValue(MemoData.class);
                    String title = memos.getTitle();
                    String date = memos.getStrDate();
                    String text = memos.getTxt();
                    memos.setKey(ds.getKey());
                    String key = memos.getKey();
                    addList(title, date, text, key);
                }
                Collections.reverse(oData);
                oAdapter = new ListAdapter(oData);
                m_oListView.setAdapter(oAdapter);
                oAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(final DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
