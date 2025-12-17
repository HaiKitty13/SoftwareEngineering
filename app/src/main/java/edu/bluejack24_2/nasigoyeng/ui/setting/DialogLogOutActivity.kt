package edu.bluejack24_2.nasigoyeng.ui.setting

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import edu.bluejack24_2.nasigoyeng.R
import edu.bluejack24_2.nasigoyeng.ui.base.BaseActivity

class DialogLogOutActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_dialog_log_out)

    }
}