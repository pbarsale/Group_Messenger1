package edu.buffalo.cse.cse486586.groupmessenger1;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.net.Uri;
import android.content.ContentResolver;
import android.content.ContentValues;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;


import android.util.Log;
import java.net.ServerSocket;


/**
 * GroupMessengerActivity is the main Activity for the assignment.
 * 
 * @author stevko
 *
 */
public class GroupMessengerActivity extends Activity {

    static final String TAG = GroupMessengerActivity.class.getSimpleName();
    static final String[] REMOTE_PORTS = {"11108","11112","11116","11120","11124"};
    static final int SERVER_PORT = 10000;
    static int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_messenger);

        /*
         * TODO: Use the TextView to display your messages. Though there is no grading component
         * on how you display the messages, if you implement it, it'll make your debugging easier.
         */

        TelephonyManager tel = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
        final String myPort = String.valueOf((Integer.parseInt(portStr) * 2));

        try
        {
            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);
        }
        catch (Exception e)
        {
            Log.e(TAG, "Can't create a ServerSocket");
            return;
        }

        final EditText editText = (EditText) findViewById(R.id.editText1);

        TextView tv = (TextView) findViewById(R.id.textView1);
        tv.setMovementMethod(new ScrollingMovementMethod());
        
        /*
         * Registers OnPTestClickListener for "button1" in the layout, which is the "PTest" button.
         * OnPTestClickListener demonstrates how to access a ContentProvider.
         */
        findViewById(R.id.button1).setOnClickListener(

                new OnPTestClickListener(tv, getContentResolver()));
        
        /*
         * TODO: You need to register and implement an OnClickListener for the "Send" button.
         * In your implementation you need to get the message from the input box (EditText)
         * and send it to other AVDs.
         */
        findViewById(R.id.button4).setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    String msg = editText.getText().toString() + "\n";
                    editText.setText(""); // This is one way to reset the input box.
                    new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, msg, myPort);

                }
            });
        }


    private class ServerTask extends AsyncTask<ServerSocket, String, Void> {

        private Uri mUri;
        private ContentValues mContentValues;

        @Override
        protected Void doInBackground(ServerSocket... sockets) {

            //  Log.d(TAG, "doInBackground: Starting the code");
            //  Log.d(TAG, "doInBackground: Socket accept done");
            //  dataInputStream = new DataInputStream(socket.getInputStream());

         //   Log.d(TAG, "doInBackground: Starting the code");
            ServerSocket serverSocket = sockets[0];
            Socket socket = null;
            try {
                while (true) {

                  //  Log.d(TAG, "doInBackground: In try");
                    // Server will accept the connection from the client
                    socket = serverSocket.accept();
                  //  Log.d(TAG, "doInBackground: Accepted");

                    // This will read the message sent on the InputStream
                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(socket.getInputStream()));

                    // Read the message line by line
                    String line = in.readLine();
                  //  Log.d(TAG, "doInBackground: Read the line");
                  //  Log.d(TAG, "doInBackground: Line " + line);
                    if (line != null) {
                     //   Log.d(TAG, "doInBackground: Line is not null");
                        mUri = buildUri("content", "edu.buffalo.cse.cse486586.groupmessenger1.provider");
                        mContentValues = new ContentValues();
                        mContentValues.put("key", Integer.toString(count));
                        count++;
                        mContentValues.put("value", line);
                        getContentResolver().insert(mUri, mContentValues);

                      //  Log.d(TAG, "doInBackground: Stored in DB");
                    }
                    in.close();
                }

            } catch (Exception e) {
                Log.e(TAG, "Unknown error. Please try again");
            }

            /*
             * TODO: Fill in your server code that receives messages and passes them
             * to onProgressUpdate().
             */
            return null;
        }
    }

    private class ClientTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... msgs) {
            try {

                for(int i=0;i<REMOTE_PORTS.length;i++)
                {
                    String remotePort = REMOTE_PORTS[i];

                    Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                            Integer.parseInt(remotePort));

                    String msgToSend = msgs[0];

                  //  Log.d(TAG, "Client: " + msgToSend);

                    // PrintWriter will send the message to the IP binded to the socket
                    PrintWriter out =
                            new PrintWriter(socket.getOutputStream(), true);


                   // Log.d(TAG, "Client: PrintWriter Created");
                    out.println(msgToSend);
                    out.flush();
                   // Log.d(TAG, "Client: Sent to server");
                /*
                 * TODO: Fill in your client code that sends out a message.
                 */
                }

            } catch (UnknownHostException e) {
                Log.e(TAG, "ClientTask UnknownHostException");
            } catch (IOException e) {
                Log.e(TAG, "ClientTask socket IOException");
            }

            return null;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_group_messenger, menu);
        return true;

    }

    private Uri buildUri(String scheme, String authority) {
        Uri.Builder uriBuilder = new Uri.Builder();
        uriBuilder.authority(authority);
        uriBuilder.scheme(scheme);
        return uriBuilder.build();
    }



}
