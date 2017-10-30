package es.elb4t.masterlistas

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_inicio_sesion.*


class InicioSesionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inicio_sesion)
    }

    fun loguearCheckbox(v: View) {
        val s = "Recordar datos de usuario: " + if (recordarme.isChecked) "Sí" else "No"
        Toast.makeText(this, s, Toast.LENGTH_LONG).show()
    }
}
