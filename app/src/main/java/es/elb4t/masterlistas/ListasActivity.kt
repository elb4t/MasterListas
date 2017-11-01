package es.elb4t.masterlistas

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.util.Pair
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.transition.TransitionInflater
import android.view.View
import android.widget.Toast
import com.mxn.soul.flowingdrawer_core.ElasticDrawer
import com.mxn.soul.flowingdrawer_core.FlowingDrawer
import kotlinx.android.synthetic.main.content_listas.*








class ListasActivity : AppCompatActivity() {

    private lateinit var mDrawer: FlowingDrawer
    private lateinit var adapter: RecyclerView.Adapter<*>
    private lateinit var lManager: RecyclerView.LayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_listas)

        // Toolbar
        val toolbar = findViewById<View>(R.id.detail_toolbar) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // Navigation drawer
        val navigationView = findViewById<View>(R.id.vNavigation) as NavigationView
        navigationView.setNavigationItemSelectedListener { menuItem ->
            Toast.makeText(applicationContext, menuItem.title, Toast.LENGTH_SHORT).show()
            false
        }
        mDrawer = findViewById<View>(R.id.drawerlayout) as FlowingDrawer
        mDrawer.setTouchMode(ElasticDrawer.TOUCH_MODE_BEZEL)
        toolbar.setNavigationIcon(R.drawable.ic_menu_white_24dp)
        toolbar.setNavigationOnClickListener { mDrawer.toggleMenu() }

        // Fab button
        fab.setOnClickListener(View.OnClickListener { view ->
            Snackbar.make(view, "Se presionó el FAB", Snackbar.LENGTH_LONG).show()
        })

        //Inicializar los elementos
        var items = ArrayList<Lista>()
        items.add(Lista(R.drawable.trabajo, "Trabajo", 2))
        items.add(Lista(R.drawable.casa, "Personal", 3))

        // Obtener el Recycler
        reciclador.setHasFixedSize(true)

        // Usar un administrador para LinearLayout
        lManager = LinearLayoutManager(this)
        reciclador.layoutManager = lManager

        // Crear nuevo adaptador
        adapter = ListaAdapter(items)
        reciclador.adapter = adapter

        reciclador.addOnItemTouchListener(RecyclerItemClickListener(applicationContext, object : RecyclerItemClickListener.OnItemClickListener {
            override fun onItemClick(v: View, position: Int) {
                val intent = Intent(applicationContext, DetalleListaActivity::class.java)
                intent.putExtra("numeroLista", position)

                val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        this@ListasActivity,
                        Pair<View,String>(v.findViewById(R.id.imagen), getString(R.string.transition_name_img)),
                        Pair<View,String>(fab, getString(R.string.transition_name_boton))
                )
                startActivity(intent, options.toBundle())
            }
        }))

        var lista_enter = TransitionInflater.from(this).inflateTransition(R.transition.transition_lista_enter)
        getWindow().setEnterTransition(lista_enter);

    }

   /* override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            nav_1 -> {
                Toast.makeText(this, "Opción 1", Toast.LENGTH_SHORT).show()
            }
            nav_2 -> Toast.makeText(this, "opción 2", Toast.LENGTH_SHORT).show()
            nav_3 -> Toast.makeText(this, "Opción 3", Toast.LENGTH_SHORT).show()
        }
        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }*/

    override fun onBackPressed() {
        if (mDrawer.isMenuVisible()) {
            mDrawer.closeMenu()
        } else {
            super.onBackPressed()
        }
    }
}
