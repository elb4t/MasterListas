package es.ellacer.masterlistas

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.ImageView
import kotlinx.android.synthetic.main.content_listas.*

class DetalleListaActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_lista)

        val numeroLista = intent.extras.getSerializable("numeroLista")
        detail_toolbar.setTitleTextColor(resources.getColor(R.color.textWhite))
        detail_toolbar.title = ""

        val imageView = findViewById<View>(R.id.imagen) as ImageView
        if (numeroLista == 0){
            detail_toolbar.title = getString(R.string.trabajo)
            imageView.setImageResource(R.drawable.trabajo)
        } else {
            detail_toolbar.title = getString(R.string.personal)
            imageView.setImageResource(R.drawable.casa)
        }
    }
}
