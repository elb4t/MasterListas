package es.ellacer.masterlistas

import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.app.ActivityOptionsCompat
import android.support.v4.content.FileProvider
import android.support.v4.util.Pair
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.transition.TransitionInflater
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import com.facebook.ads.*
import com.facebook.ads.AdSize
import com.google.android.gms.ads.*
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.reward.RewardItem
import com.google.android.gms.ads.reward.RewardedVideoAd
import com.google.android.gms.ads.reward.RewardedVideoAdListener
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.mxn.soul.flowingdrawer_core.ElasticDrawer
import com.mxn.soul.flowingdrawer_core.FlowingDrawer
import kotlinx.android.synthetic.main.content_listas.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class ListasActivity : AppCompatActivity(), RewardedVideoAdListener, InterstitialAdListener {

    private lateinit var mDrawer: FlowingDrawer
    private lateinit var adapter: RecyclerView.Adapter<*>
    private lateinit var lManager: RecyclerView.LayoutManager

    private lateinit var mFirebaseAnalytics: FirebaseAnalytics
    private lateinit var remoteConfig: FirebaseRemoteConfig
    private val CACHE_TIME_SECONDS: Long = 30

    private lateinit var adView: AdView
    private lateinit var intersticialAd: InterstitialAd
    private lateinit var mRewardedVideoAd: RewardedVideoAd
    private lateinit var adViewFacebook: com.facebook.ads.AdView
    private lateinit var intersticialFacebookAd: com.facebook.ads.InterstitialAd
    private lateinit var nativeFacebookAd: com.facebook.ads.NativeAd

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_listas)

        // Intersticial Ad
        intersticialAd = InterstitialAd(this)
        intersticialAd.adUnitId = getString(R.string.adMobIdIntersticial)
        intersticialAd.loadAd(AdRequest.Builder().build())

        intersticialAd.adListener = object : AdListener() {
            override fun onAdClosed() {
                intersticialAd.loadAd(AdRequest.Builder().addTestDevice("EF7FB31EE1E155863F06CF1D12FB1B68").build())
            }
        }

        // Cross promotion
        showCrossPromoDialog()

        // Banner Ad
        adView = findViewById(R.id.adView)
        val adRequest = AdRequest.Builder().addTestDevice("EF7FB31EE1E155863F06CF1D12FB1B68").build()
        adView.loadAd(adRequest)

        // Video reward
        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this)
        mRewardedVideoAd.rewardedVideoAdListener = this
        mRewardedVideoAd.loadAd(getString(R.string.adMobIdVideoBonificado), AdRequest.Builder().addTestDevice("EF7FB31EE1E155863F06CF1D12FB1B68").build())

        // Banner Facebook
        crearAnuncioBannerFacebook()
        crearAnuncioNativoFacebook()

        // Firebase Analytics
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)

        // Toolbar
        val toolbar = findViewById<View>(R.id.detail_toolbar) as Toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // Navigation drawer
        val navigationView = findViewById<View>(R.id.vNavigation) as NavigationView
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_1 -> {
                    if (mRewardedVideoAd.isLoaded)
                        mRewardedVideoAd.show()
                }
                R.id.nav_2 -> crearAnuncioIntersticialFacebook()
                R.id.nav_compartir -> compatirTexto("http://play.google.com/store/apps/details?id=" + packageName)
                R.id.nav_compartir_lista -> compatirTexto("LISTA DE LA COMPRA: patatas, leche, huevos. ---- Compartido por: http://play.google.com/store/apps/details?id=" + packageName)
                R.id.nav_compartir_logo -> {
                    var bitmap = BitmapFactory.decodeResource(resources, R.drawable.logo)
                    compatirBitmap(bitmap, "Compartido por: " + "http://play.google.com/store/apps/details?id=" + packageName)
                }
                R.id.nav_compartir_desarrollador -> compatirTexto("https://play.google.com/store/apps/dev?id=7027410910970713274")
                else -> Toast.makeText(applicationContext, menuItem.title, Toast.LENGTH_SHORT).show()
            }
            false
        }
        mDrawer = findViewById<View>(R.id.drawerlayout) as FlowingDrawer
        mDrawer.setTouchMode(ElasticDrawer.TOUCH_MODE_BEZEL)
        toolbar.setNavigationIcon(R.drawable.ic_menu_white_24dp)
        toolbar.setNavigationOnClickListener {
            mDrawer.toggleMenu()
            mFirebaseAnalytics.logEvent("Navigation_Drawer", Bundle())
            Toast.makeText(this, "Nav", Toast.LENGTH_SHORT).show()
        }

        // Fab button
        fab.setOnClickListener { view ->
            if (intersticialAd.isLoaded) {
                intersticialAd.show()
            } else {
                Toast.makeText(this, "El Anuncio no esta disponible aun", Toast.LENGTH_LONG).show()
            }
        }

        //Inicializar los elementos
        var items = ArrayList<Lista>()
        items.add(Lista(R.drawable.trabajo, getString(R.string.trabajo), 2))
        items.add(Lista(R.drawable.casa, getString(R.string.personal), 3))

        // Obtener el Recycler
        reciclador.setHasFixedSize(true)

        // Usar un administrador para LinearLayout
        lManager = LinearLayoutManager(this) as RecyclerView.LayoutManager
        reciclador.layoutManager = lManager

        // Crear nuevo adaptador
        adapter = ListaAdapter(items, applicationContext)
        reciclador.adapter = adapter

        reciclador.addOnItemTouchListener(RecyclerItemClickListener(applicationContext, object : RecyclerItemClickListener.OnItemClickListener {
            override fun onItemClick(v: View, position: Int) {
                val intent = Intent(applicationContext, DetalleListaActivity::class.java)
                intent.putExtra("numeroLista", position)

                val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        this@ListasActivity,
                        Pair<View, String>(v.findViewById(R.id.imagen), getString(R.string.transition_name_img)),
                        Pair<View, String>(fab, getString(R.string.transition_name_boton))
                )
                startActivity(intent, options.toBundle())
            }
        }))

        var lista_enter = TransitionInflater.from(this).inflateTransition(R.transition.transition_lista_enter)
        getWindow().setEnterTransition(lista_enter);

        remoteConfig = FirebaseRemoteConfig.getInstance()
        val config = FirebaseRemoteConfigSettings.Builder().setDeveloperModeEnabled(BuildConfig.DEBUG).build()
        remoteConfig.setConfigSettings(config)
        remoteConfig.fetch(CACHE_TIME_SECONDS)
                .addOnCompleteListener(this, OnCompleteListener<Void> { task ->
                    if (task.isSuccessful) {
                        remoteConfig.activateFetched()
                        val navigation_abierto = remoteConfig.getString("navigation_drawer_abierto")
                        mFirebaseAnalytics.setUserProperty("nav_drawer_abierto", navigation_abierto.toString())
                        Log.e("-----", navigation_abierto)
                        if (navigation_abierto == "true") {
                            abrePrimeraVez()
                        }
                    }
                })

        RateMyApp(this).app_launched()
    }


    override fun onBackPressed() {
        if (mDrawer.isMenuVisible()) {
            mDrawer.closeMenu()
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        if (adViewFacebook != null)
            adViewFacebook.destroy()
        super.onDestroy()
    }

    fun abrePrimeraVez() {
        val sp = getSharedPreferences("mispreferencias", 0)
        val primerAcceso = sp.getBoolean("abrePrimeraVez", true)
        mFirebaseAnalytics.setUserProperty("abrePrimeraVez", primerAcceso.toString())
        if (primerAcceso) {
            mDrawer.openMenu()
            val e = sp.edit()
            e.putBoolean("abrePrimeraVez", false).commit()
        }
    }

    fun compatirTexto(texto: String) {
        val i = Intent(Intent.ACTION_SEND)
        i.type = "text/plain"
        i.putExtra(Intent.EXTRA_TEXT, texto)
        startActivity(Intent.createChooser(i, "Selecciona aplicación"))
    }

    fun compatirBitmap(bitmap: Bitmap, texto: String) {
        // guardamos bitmap en el directorio cache
        try {
            var cachePath = File(cacheDir, "images")
            cachePath.mkdirs()
            var s = FileOutputStream("$cachePath/image.png")
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, s)
            s.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        // Obtenemos la URI usando el FileProvider
        var path = File(getCacheDir(), "images")
        var file = File(path, "image.png")
        var uri = FileProvider.getUriForFile(this, "es.ellacer.masterlistas.fileprovider", file)
        //Compartimos la URI
        if (uri != null) {
            var i = Intent(Intent.ACTION_SEND)
            i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            // temp permission for receiving app to read this file
            i.setDataAndType(uri, getContentResolver().getType(uri))
            i.putExtra(Intent.EXTRA_STREAM, uri)
            i.putExtra(Intent.EXTRA_TEXT, texto)
            startActivity(Intent.createChooser(i, "Selecciona aplicación"))
        }
    }

    private fun showCrossPromoDialog() {
        val dialog = Dialog(this, R.style.Theme_AppCompat)
        dialog.setContentView(R.layout.dialog_crosspromotion)
        dialog.setCancelable(true)
        val buttonCancel: Button = dialog.findViewById(R.id.buttonCancel)
        buttonCancel.setOnClickListener { dialog.dismiss() }
        val boton: Button = dialog.findViewById(R.id.buttonDescargar)
        boton.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?" + "id=com.mimisoftware.emojicreatoremoticonosemoticones")))
            dialog.dismiss()
        }
        dialog.show()
    }

    fun crearAnuncioBannerFacebook() {
        adViewFacebook = com.facebook.ads.AdView(this, getString(R.string.idBannerFacebook), AdSize.BANNER_320_50)
        AdSettings.addTestDevice("8613d68ce24be2b3e69131393d439671")
        AdSettings.addTestDevice("54702c096a73443cc79a60965f5fc7da")
        adViewContainer.addView(adViewFacebook)
        adViewFacebook.loadAd()
    }

    fun crearAnuncioIntersticialFacebook() {
        intersticialFacebookAd = com.facebook.ads.InterstitialAd(this, getString(R.string.idIntersticialFacebook))
        AdSettings.addTestDevice("8613d68ce24be2b3e69131393d439671")
        AdSettings.addTestDevice("54702c096a73443cc79a60965f5fc7da")
        intersticialFacebookAd.setAdListener(this)
        intersticialFacebookAd.loadAd()
    }

    fun crearAnuncioNativoFacebook(){
        nativeFacebookAd = NativeAd(this, getString(R.string.idNativoFacebook))
        nativeFacebookAd.setAdListener(object : com.facebook.ads.AdListener{
            override fun onAdClicked(p0: Ad?) {}
            override fun onError(p0: Ad?, p1: AdError?) {}
            override fun onAdLoaded(p0: Ad?) {
                if (nativeFacebookAd != null)
                    nativeFacebookAd.unregisterView()

                val nativeAdContainer: LinearLayout = findViewById(R.id.native_ad_container)
                val inflater: LayoutInflater = LayoutInflater.from(applicationContext)
                val adView: LinearLayout = inflater.inflate(R.layout.native_ad, nativeAdContainer, false) as LinearLayout
                nativeAdContainer.addView(adView)

                val nativeAdIcon: ImageView = findViewById(R.id.native_ad_icon)
                val nativeAdTitle: TextView = findViewById(R.id.native_ad_title)
                val nativeAdMadia: MediaView = findViewById(R.id.native_ad_media)
                val nativeAdSocialCpntext: TextView = findViewById(R.id.native_ad_social_context)
                val nativeAdBody: TextView = findViewById(R.id.native_ad_body)
                val nativeAdCallToAction: Button = findViewById(R.id.native_ad_call_to_action)

                nativeAdTitle.text = nativeFacebookAd.adTitle
                nativeAdSocialCpntext.text = nativeFacebookAd.adSocialContext
                nativeAdBody.text = nativeFacebookAd.adBody
                val adIcon: NativeAd.Image = nativeFacebookAd.adIcon
                NativeAd.downloadAndDisplayImage(adIcon, nativeAdIcon)
                nativeAdMadia.setNativeAd(nativeFacebookAd)

                val adChoisesContainer: LinearLayout = findViewById(R.id.ad_choices_container)
                val adChoisesView: AdChoicesView = AdChoicesView(applicationContext, nativeFacebookAd, true)
                adChoisesContainer.addView(adChoisesView)

                var clickableView: ArrayList<View> = ArrayList()
                clickableView.add(nativeAdTitle)
                clickableView.add(nativeAdCallToAction)

                nativeFacebookAd.registerViewForInteraction(nativeAdContainer, clickableView)
            }
            override fun onLoggingImpression(p0: Ad?) {}
        })
        nativeFacebookAd.loadAd()
    }

    // Metodos listener intersticial Facebook
    override fun onError(p0: Ad?, adError: AdError) {
        Toast.makeText(this, "Error: ${adError.errorMessage}", Toast.LENGTH_LONG).show()
    }

    override fun onAdLoaded(p0: Ad?) {
        intersticialFacebookAd.show()
    }
    override fun onInterstitialDisplayed(p0: Ad?) {
    }

    override fun onAdClicked(p0: Ad?) {
    }

    override fun onInterstitialDismissed(p0: Ad?) {
    }

    override fun onLoggingImpression(p0: Ad?) {
    }

    // Metodos listener video rewarded
    override fun onRewardedVideoAdLoaded() {
        Toast.makeText(this, "Vídeo Bonificado cargado", Toast.LENGTH_SHORT).show()
    }

    override fun onRewardedVideoAdClosed() {
        mRewardedVideoAd.loadAd(getString(R.string.adMobIdVideoBonificado), AdRequest.Builder().build())
    }

    override fun onRewarded(rewardItem: RewardItem) {
        Toast.makeText(this, "onRewarded: moneda virtual: ${rewardItem.type}  aumento: ${rewardItem.amount}", Toast.LENGTH_SHORT).show()
    }

    override fun onRewardedVideoAdLeftApplication() {
    }

    override fun onRewardedVideoAdOpened() {
    }

    override fun onRewardedVideoStarted() {
    }

    override fun onRewardedVideoAdFailedToLoad(p0: Int) {
    }
}
