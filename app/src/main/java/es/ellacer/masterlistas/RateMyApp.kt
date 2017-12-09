package es.ellacer.masterlistas

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.widget.Button


/**
 * Created by eloy on 9/12/17.
 */
class RateMyApp {
    private val APP_PACKAGENAME: String
    //Dias necesarios para que invoque al RateMyApp
    private val DAYS_UNTIL_PROMPT = 1
    //Veces que lanza la app para que invoque al RateMyApp
    private val LAUNCHES_UNTIL_PROMPT = 3
    private var launch_count: Long = 0 //Contador de lanzamientos
    //Hasta que no se cumplen los dias y lanzamientos no lanza RateMyApp
    private val DAYS_AND_LAUNCHES = false
    // Configurado para que al 3 lanzamiento muestre RateMyApp
    private var exceedsSpecifiedLaunches: Boolean = false
    private var callerActivity: Activity
    private lateinit var ratemyappDialog: AlertDialog

    constructor(activity: Activity) {
        callerActivity = activity
        APP_PACKAGENAME = activity.packageName
    }

    fun app_launched() {
        var prefs: SharedPreferences = callerActivity.getSharedPreferences("adapter", 0)

        if (prefs.getBoolean("dontshowagain", false))
            return

        var editor: SharedPreferences.Editor = prefs.edit()
        launch_count = prefs.getLong("launch_count", 0) + 1
        editor.putLong("launch_count", launch_count)
        var date_firtsLaunch: Long = prefs.getLong("date_firstlaunch", 0)
        if (date_firtsLaunch == 0L) {
            date_firtsLaunch = System.currentTimeMillis()
            editor.putLong("date_firstlaunch", date_firtsLaunch)
        }
        exceedsSpecifiedLaunches = launch_count >= LAUNCHES_UNTIL_PROMPT
        var exceedsDaysSynceFirtsLaunch: Boolean = System.currentTimeMillis() >= date_firtsLaunch + (DAYS_UNTIL_PROMPT * 24 * 60 * 60 * 1000)
        if ((exceedsSpecifiedLaunches || exceedsDaysSynceFirtsLaunch) && !DAYS_AND_LAUNCHES) {
            editor.putLong("launch_count", 0)
            showRateDialog(editor)
        } else if (exceedsSpecifiedLaunches && exceedsDaysSynceFirtsLaunch && DAYS_AND_LAUNCHES) {
            editor.putLong("launch_count", 0)
            showRateDialog(editor)
        }
        editor.apply()
    }

    fun showRateDialog(editor: SharedPreferences.Editor) {
        if (APP_PACKAGENAME.equals("")) return
        val builder: AlertDialog.Builder = AlertDialog.Builder(callerActivity)
        val inflater = callerActivity.layoutInflater
        val layout = inflater.inflate(R.layout.dialog_ratemyapp, null)
        val rateButton: Button = layout.findViewById(R.id.ratemyapp_dialog_accept_button)
        val cancelButton: Button = layout.findViewById(R.id.ratemyapp_dialog_cancel_button)
        val laterButton: Button = layout.findViewById(R.id.ratemyapp_dialog_later_button)
        rateButton.setOnClickListener {
            callerActivity.startActivity(Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + APP_PACKAGENAME)))
            if (editor != null) {
                editor.putBoolean("dontshowagain", true)
                editor.apply()
            }
            ratemyappDialog.dismiss()
        }
        cancelButton.setOnClickListener {
            if (editor != null) {
                editor.putLong("launch_count", 0)
                editor.apply()
            }
            var intent = Intent(Intent.ACTION_SEND)
            intent.setType("text/html")
            intent.putExtra(Intent.EXTRA_EMAIL, "empresa@gmail.com")
            intent.putExtra(Intent.EXTRA_SUBJECT, "MasterListas Issue: ")
            intent.putExtra(Intent.EXTRA_TEXT, "Put inside the issue")
            callerActivity.startActivity(Intent.createChooser(intent, "Send Email"))
            ratemyappDialog.dismiss()
        }
        laterButton.setOnClickListener {
            if (editor != null) {
                editor.putLong("launch_count", 0)
                editor.apply()
            }
            ratemyappDialog.dismiss()
        }
        builder.setView(layout)
        ratemyappDialog = builder.create()
        ratemyappDialog.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        ratemyappDialog.show()
    }

}