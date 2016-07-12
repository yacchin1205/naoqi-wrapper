package com.github.yacchin1205.naoqi_wrapper;

import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.aldebaran.qi.AnyObject;
import com.aldebaran.qi.Future;
import com.aldebaran.qi.QiCallback;
import com.aldebaran.qi.QiFunction;
import com.aldebaran.qi.Session;
import com.aldebaran.qi.sdk.Qi;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.requirement.SessionRequirement;
import com.github.yacchin1205.naoqi_wrapper.io.MemberDef;
import com.github.yacchin1205.naoqi_wrapper.io.ModuleUtils;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.List;
import java.util.concurrent.RunnableFuture;

public class MainActivity extends AppCompatActivity {

    public static final String TAG = "naoqi-wrapper";

    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ModuleUtils.init(this);
        handler = new Handler();

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        EditText moduleName = (EditText) findViewById(R.id.moduleName);
        Button button = (Button) findViewById(R.id.generate);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText moduleName = (EditText) findViewById(R.id.moduleName);
                generateWrapper(view, moduleName.getText().toString());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showError(final String phase, final Throwable error) {
        Log.e(TAG, phase, error);

        handler.post(new Runnable() {
            @Override
            public void run() {
                TextView result = (TextView)findViewById(R.id.result);
                result.setText("Error:\n" + phase + "\n" + ExceptionUtils.getStackTrace(error));
            }
        });
    }

    private void generateWrapper(final View source, final String moduleName) {
        SessionRequirement req = QiContext.get(this).getSharedRequirements().getSessionRequirement();
        req.satisfy().then(new QiFunction<AnyObject, Session>() {
            @Override
            public Future<AnyObject> onResult(Session session) throws Exception {
                return session.service(moduleName);
            }

            @Override
            public Future<AnyObject> onError(Throwable error) throws Exception {
                showError("Cannot communicate to NAOqi", error);
                return super.onError(error);
            }
        }).then(new QiFunction<List<MemberDef>, AnyObject>() {
            @Override
            public Future<List<MemberDef>> onResult(AnyObject alMemory) throws Exception {
                return ModuleUtils.loadMembers(alMemory);
            }

            @Override
            public Future<List<MemberDef>> onError(Throwable error) throws Exception {
                showError("Cannot retrieve module: " + moduleName, error);
                return super.onError(error);
            }
        }).andThen(Qi.onUiThread(new QiCallback<List<MemberDef>>() {
            @Override
            public void onResult(List<MemberDef> members) throws Exception {
                try {
                    OutputStream out = openFileOutput(moduleName + ".java", MODE_PRIVATE);
                    PrintWriter writer = new PrintWriter(new OutputStreamWriter(out, "UTF-8"));

                    VelocityContext ctx = new VelocityContext();
                    ctx.put("name", moduleName);
                    Velocity.getTemplate("header").merge(ctx, writer);

                    for (MemberDef member : members) {
                        Log.i(TAG, "Generating: " + member);
                        writer.println();
                        member.write(writer);
                    }

                    Velocity.getTemplate("footer").merge(ctx, writer);
                    writer.close();

                    String javaPath = "/data/data/com.github.yacchin1205.naoqi_wrapper/files/" + moduleName + ".java";
                    TextView result = (TextView)findViewById(R.id.result);
                    result.setText("Wrapper Generated: " + moduleName + "\n" +
                                   "Please pull " + javaPath + " to your computer through adb!\n\n" +
                                   String.format("%d members", members.size()));
                    Snackbar.make(source, "Wrapper Generated: " + moduleName, Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                } catch (Throwable t) {
                    Log.e("Sample", "Error", t);
                }
            }
        }));
    }
}
