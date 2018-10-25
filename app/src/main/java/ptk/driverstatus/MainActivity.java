package ptk.driverstatus;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;

import connection.Message;

public class MainActivity extends AppCompatActivity {

    private SocketAddress address = new InetSocketAddress("192.168.0.104", 9351);

    final static String PREFERENCES = "settings";
    final static String ID = "id";
    final static String NAME = "name";
    final static String STATUS = "status";
    final static String CURRENT_BUTTON = "currentButton";

    static SharedPreferences preferences;

    Toast toast;

    Button currentButton;

    String colorDefault = "#ffd6d7d7";
    String colorTrouble = "#ea603a";
    String colorOK = "#53ef77";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        preferences = getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);

        if (!preferences.contains(ID)) {
            Intent intent = new Intent(this, InfoActivity.class);
            startActivity(intent);
        } else {
            String status = preferences.getString(STATUS, "Отсутствует");
            int buttonID = preferences.getInt(CURRENT_BUTTON, 0);

            TextView textView = findViewById(R.id.status);
            textView.setText(status);

            currentButton = findViewById(buttonID);

            if (currentButton != null) {
                if (currentButton.getText().equals("проблема"))
                    currentButton.getBackground().setColorFilter(Color.parseColor(colorTrouble), PorterDuff.Mode.MULTIPLY);
                else
                    currentButton.getBackground().setColorFilter(Color.parseColor(colorOK), PorterDuff.Mode.MULTIPLY);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Заполнение меню; элементы действий добавляются на панель приложения
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_info:
                Intent intent = new Intent(this, InfoActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    public void onCLickTrouble(View view) {
        Button button = (Button) view;
        if (currentButton != null)
            currentButton.getBackground().setColorFilter(Color.parseColor(colorDefault), PorterDuff.Mode.MULTIPLY);

        currentButton = button;
        currentButton.getBackground().setColorFilter(Color.parseColor(colorTrouble), PorterDuff.Mode.MULTIPLY);

        SharedPreferences.Editor editor = MainActivity.preferences.edit();
        editor.putInt(MainActivity.CURRENT_BUTTON, button.getId());
        editor.apply();

        sendStatus(button.getText().toString());
    }

    public void onCLickOnLoading(View view) {
        Button button = (Button) view;
        sendStatus(button.getText().toString());
        changeButtonColor(button);
    }

    public void onCLickPaperWork(View view) {
        Button button = (Button) view;
        sendStatus(button.getText().toString());
        changeButtonColor(button);
    }

    public void onCLickOnTheWay(View view) {
        Button button = (Button) view;
        sendStatus(button.getText().toString());
        changeButtonColor(button);
    }

    private void changeButtonColor(Button button) {
        if (currentButton != null)
            currentButton.getBackground().setColorFilter(Color.parseColor(colorDefault), PorterDuff.Mode.MULTIPLY);

        currentButton = button;
        currentButton.getBackground().setColorFilter(Color.parseColor(colorOK), PorterDuff.Mode.MULTIPLY);

        SharedPreferences.Editor editor = MainActivity.preferences.edit();
        editor.putInt(MainActivity.CURRENT_BUTTON, button.getId());
        editor.apply();
    }

    private void sendStatus(final String status) {

        toast = Toast.makeText(this, "", Toast.LENGTH_LONG);

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

                    Message message = new Message("status");
                    message.setId(preferences.getString(ID, ""));
                    message.setStatus(status);

                    objOut.writeObject(message);
                    objOut.flush();

                    Message tmpMessage;

                    while (true) {
                        tmpMessage = (Message) objIn.readObject();
                        if (tmpMessage.getMsg().equals("statusOK"))
                            break;
                    }

                    final Message response = tmpMessage;

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            TextView textView = findViewById(R.id.status);
                            textView.setText(response.getStatus());

                            SharedPreferences.Editor editor = MainActivity.preferences.edit();
                            editor.putString(MainActivity.STATUS, response.getStatus());
                            editor.apply();

                            toast.setText("Статус успешно отправлен");
                            toast.show();
                        }
                    });

                    socket.close();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
