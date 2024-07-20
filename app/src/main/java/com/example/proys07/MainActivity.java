package com.example.proys07;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.proys07.model.Caja;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private List<Caja> listCajas = new ArrayList<>();
    ArrayAdapter<Caja> arrayAdapterCaja;

    EditText fechaP, horaP, motivoP, montoP;
    ListView listV_cajas;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    Caja cajaSelected;
    private Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fechaP = findViewById(R.id.txt_fechaCaja);
        horaP = findViewById(R.id.txt_horaCaja);
        motivoP = findViewById(R.id.txt_motivoCaja);
        montoP = findViewById(R.id.txt_montoCaja);

        listV_cajas = findViewById(R.id.lv_datosCajas);
        inicializarFirebase();
        listarDatos();

        // Establecer valores por defecto de fecha y hora
        setDefaultDateTime();

        resetMenuIcons();

        listV_cajas.setOnItemClickListener((parent, view, position, id) -> {
            cajaSelected = (Caja) parent.getItemAtPosition(position);
            fechaP.setText(cajaSelected.getFecha());
            horaP.setText(cajaSelected.getHora());
            motivoP.setText(cajaSelected.getMotivo());
            montoP.setText(cajaSelected.getMonto());

            // Habilitar y deshabilitar íconos del menú
            if (menu != null) {
                menu.findItem(R.id.icon_add).setEnabled(false);
                menu.findItem(R.id.icon_save).setVisible(true).setEnabled(true);
                menu.findItem(R.id.icon_delete).setVisible(true).setEnabled(true);
            }
        });

    }

    private void setDefaultDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());

        Date now = new Date();
        String currentDate = dateFormat.format(now);
        String currentTime = timeFormat.format(now);

        fechaP.setText(currentDate);
        horaP.setText(currentTime);
    }

    private void listarDatos() {
        databaseReference.child("Caja").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listCajas.clear();
                for (DataSnapshot objSnaptshot : dataSnapshot.getChildren()) {
                    Caja c = objSnaptshot.getValue(Caja.class);
                    listCajas.add(c);

                    arrayAdapterCaja = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, listCajas);
                    listV_cajas.setAdapter(arrayAdapterCaja);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void inicializarFirebase() {
        FirebaseApp.initializeApp(this);
        firebaseDatabase = FirebaseDatabase.getInstance();
        //firebaseDatabase.setPersistenceEnabled(true);
        databaseReference = firebaseDatabase.getReference();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        this.menu = menu;
        // Deshabilitar iconos inicialmente
        menu.findItem(R.id.icon_save).setVisible(false).setEnabled(false);
        menu.findItem(R.id.icon_delete).setVisible(false).setEnabled(false);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        String fecha = fechaP.getText().toString();
        String hora = horaP.getText().toString();
        String motivo = motivoP.getText().toString();
        String monto = montoP.getText().toString();

        switch (item.getItemId()) {
            case R.id.icon_add: {
                if (fecha.equals("") || hora.equals("") || motivo.equals("") || monto.equals("")) {
                    validacion();
                } else {
                    Caja c = new Caja();
                    c.setUid(UUID.randomUUID().toString());
                    c.setFecha(fecha);
                    c.setHora(hora);
                    c.setMotivo(motivo);
                    c.setMonto(monto);
                    databaseReference.child("Caja").child(c.getUid()).setValue(c);
                    Toast.makeText(this, "Agregado", Toast.LENGTH_LONG).show();
                    limpiarCajas();
                    setDefaultDateTime();
                    resetMenuIcons();
                }
                break;
            }
            case R.id.icon_save: {
                Caja c = new Caja();
                c.setUid(cajaSelected.getUid());
                c.setFecha(fechaP.getText().toString().trim());
                c.setHora(horaP.getText().toString().trim());
                c.setMotivo(motivoP.getText().toString().trim());
                c.setMonto(montoP.getText().toString().trim());
                databaseReference.child("Caja").child(c.getUid()).setValue(c);
                Toast.makeText(this, "Actualizado", Toast.LENGTH_LONG).show();
                limpiarCajas();
                setDefaultDateTime();
                resetMenuIcons();
                break;
            }
            case R.id.icon_delete: {
                Caja c = new Caja();
                c.setUid(cajaSelected.getUid());
                databaseReference.child("Caja").child(c.getUid()).removeValue();
                Toast.makeText(this, "Eliminado", Toast.LENGTH_LONG).show();
                limpiarCajas();
                setDefaultDateTime();
                resetMenuIcons();
                break;
            }
            default:
                break;
        }
        return true;

    }

    private void resetMenuIcons() {
        if (menu != null) {
            menu.findItem(R.id.icon_add).setEnabled(true);
            menu.findItem(R.id.icon_save).setVisible(false).setEnabled(false);
            menu.findItem(R.id.icon_delete).setVisible(false).setEnabled(false);
        }
    }

    private void limpiarCajas() {
        fechaP.setText("");
        horaP.setText("");
        motivoP.setText("");
        montoP.setText("");
    }

    private void validacion() {
        String fecha = fechaP.getText().toString();
        String hora = horaP.getText().toString();
        String motivo = motivoP.getText().toString();
        String monto = montoP.getText().toString();
        if (fecha.equals("")) {
            fechaP.setError("Required");
        } else if (hora.equals("")) {
            horaP.setError("Required");
        } else if (motivo.equals("")) {
            motivoP.setError("Required");
        } else if (monto.equals("")) {
            montoP.setError("Required");
        }
    }
}//Fin clase
