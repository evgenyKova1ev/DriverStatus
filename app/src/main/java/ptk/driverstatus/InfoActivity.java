package ptk.driverstatus;

import android.content.SharedPreferences;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import connection.Message;

public class InfoActivity extends AppCompatActivity {

    private SocketAddress address = new InetSocketAddress("192.168.0.104", 9351);

    ArrayAdapter<String> adapter;
    Spinner spinner;
    Button saveButton;

    Map<String, String> map;
    List<String> list;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        Toolbar toolbar = findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        TextView surname = findViewById(R.id.currentSurname);
        TextView name = findViewById(R.id.currentName);
        TextView patronymic = findViewById(R.id.currentPatronymic);

        if (MainActivity.preferences.contains(MainActivity.NAME)) {
            String[] fullName = MainActivity.preferences.getString(MainActivity.NAME, "").split(" ");
            surname.setText(fullName[0]);
            name.setText(fullName[1]);
            patronymic.setText(fullName[2]);
        }
    }

    public void onClickChange(View view) {

        adapter = new ArrayAdapter<>(this, R.layout.spinner_item);

        new Thread(new Runnable() {
            @Override
            public void run() {
                Socket socket = new Socket();

                try {
                    socket.connect(address);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try (ObjectOutputStream objOut = new ObjectOutputStream(socket.getOutputStream());
                     ObjectInputStream objIn = new ObjectInputStream(socket.getInputStream())) {

                    objOut.writeObject(new Message("driversForAndroid"));
                    objOut.flush();

                    Message message;

                    while (true) {
                        message = (Message) objIn.readObject();
                        if (message.getMsg().equals("drivers"))
                            break;
                    }

                    if (map != null)
                        return;

                    map = new HashMap<>();

                    for (Message.Data data : message.getData()) {
                        map.put(data.getName(), data.getId());
                    }

                    list = new ArrayList<>(map.keySet());

                    Collections.sort(list);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            spinner = findViewById(R.id.spinner);
                            adapter.addAll(list);
                            spinner.setAdapter(adapter);
                            spinner.setVisibility(View.VISIBLE);
                            saveButton = findViewById(R.id.saveButton);
                            saveButton.setVisibility(View.VISIBLE);
                        }
                    });

                    socket.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void onClickSave(View view) {
        String selection = String.valueOf(spinner.getSelectedItem());

        SharedPreferences.Editor editor = MainActivity.preferences.edit();
        editor.putString(MainActivity.ID, map.get(selection));
        editor.putString(MainActivity.NAME, selection);
        editor.apply();

        TextView name = findViewById(R.id.currentName);
        TextView surname = findViewById(R.id.currentSurname);
        TextView patronymic = findViewById(R.id.currentPatronymic);

        String[] tmp = selection.split(" ");

        surname.setText(tmp[0]);
        name.setText(tmp[1]);
        patronymic.setText(tmp[2]);

        spinner.setVisibility(View.INVISIBLE);
        saveButton.setVisibility(View.INVISIBLE);

        Toast toast = Toast.makeText(this, "Данные успешно сохранены.", Toast.LENGTH_LONG);
        toast.show();
    }

}
