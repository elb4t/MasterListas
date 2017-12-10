package es.ellacer.masterlistas

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.android.gms.ads.MobileAds
import kotlinx.android.synthetic.main.activity_inicio_sesion.*




class InicioSesionActivity : AppCompatActivity() {

    var bloqueo: Array<Any?> = arrayOfNulls(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inicio_sesion)

        MobileAds.initialize(this,getString(R.string.adMobIdApp))

        boton_facebook.setOnClickListener { incrementaIndiceDeBloqueo(it) }
        boton_google.setOnClickListener { incrementaIndiceDeANR(it) }
    }

    fun loguearCheckbox(v: View) {
        val s = getString(R.string.recordar_datos) + if (recordarme.isChecked) android.R.string.yes else android.R.string.no
        Toast.makeText(this, s, Toast.LENGTH_LONG).show()
    }

    fun mostrarContrasena(v: View) {
        if (mostrar_contrasena.isChecked) {
            contrasena.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_NORMAL

        } else {
            contrasena.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
    }

    fun acceder(view: View) {
        val intent = Intent(this, ListasActivity::class.java)
        startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle())
    }

    fun borrarCampos(view: View) {
        usuario.setText("")
        contrasena.setText("")
        usuario.requestFocus()
    }

    fun registrarUsuario(view: View) {
        val intent = Intent(this, RegistroUsuarioActivity::class.java)
        startActivity(intent)
    }


    fun incrementaIndiceDeBloqueo(view: View) {
        bloqueo.set(10,{null})
        Log.e("bloqueo","-------")
    }

    fun incrementaIndiceDeANR(view: View) {
        try {
            Thread.sleep(10000)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }
}

