package es.ellacer.masterlistas

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.InputType
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_registro_usuario.*

class RegistroUsuarioActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registro_usuario)
    }

    fun mostrarContraseña(v: View) {
        if (reg_mostrar_contrasena.isChecked) {
            reg_contrasena.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_NORMAL
            reg_verificar_contrasena.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_NORMAL

        } else {
            reg_contrasena.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            reg_verificar_contrasena.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
    }

    fun registro(view: View) {
        Toast.makeText(this, getString(R.string.usuario_registrado),Toast.LENGTH_SHORT).show()
        val intent = Intent(this, ListasActivity::class.java)
        startActivity(intent)
    }

    fun borrarCampos(view: View) {
        usuario.setText("")
        reg_contrasena.setText("")
        reg_verificar_contrasena.setText("")
        usuario.requestFocus()
    }

}
