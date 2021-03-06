package dk.nodes.locksmith.example.activities

import android.os.Build
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import dk.nodes.locksmith.example.R
import dk.nodes.locksmith.example.fragments.EncryptionFragment
import dk.nodes.locksmith.example.fragments.FingerprintEncryptionFragment
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), TabLayout.OnTabSelectedListener {
    private val tag = MainActivity::class.simpleName
    private val tabEncryption = "Encryption"
    private val tabFingerprintEncryption = "Fingerprint Encryption"

    private val encryptionFragment: EncryptionFragment by lazy {
        EncryptionFragment()
    }
    private val fingerprintEncryptionFragment: FingerprintEncryptionFragment by lazy {
        FingerprintEncryptionFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(mainToolbar)
        setupTabHost()

        showEncryptionFragment()
    }

    private fun setupTabHost() {
        val encryptionTab = mainTabLayout.newTab()
        encryptionTab.text = tabEncryption
        encryptionTab.tag = tabEncryption
        mainTabLayout.addTab(encryptionTab)

        // If we're using above version 23 then add the fingerprint encryption tab
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val fingerprintTab = mainTabLayout.newTab()
            fingerprintTab.text = tabFingerprintEncryption
            fingerprintTab.tag = tabFingerprintEncryption
            mainTabLayout.addTab(fingerprintTab)
        }

        mainTabLayout.addOnTabSelectedListener(this)
    }

    private fun showEncryptionFragment() {
        Log.d(tag, "showEncryptionFragment")
        setCurrentFragment(encryptionFragment)
    }

    private fun showFingerprintEncryptionDialog() {
        Log.d(tag, "showFingerprintEncryptionDialog")
        setCurrentFragment(fingerprintEncryptionFragment)
    }


    private fun setCurrentFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
                .replace(R.id.mainFragmentContainer, fragment, fragment.javaClass.simpleName)
                .commit()
    }

    override fun onTabSelected(tab: TabLayout.Tab) {
        Log.d(tag, "onTabSelected ${tab.text}")

        when (tab.tag) {
            tabEncryption            -> showEncryptionFragment()
            tabFingerprintEncryption -> showFingerprintEncryptionDialog()
            else                     -> {
                Log.d(tag, "Else tab selected")
            }
        }
    }

    override fun onTabReselected(tab: TabLayout.Tab?) {
        // Do nothing
    }

    override fun onTabUnselected(tab: TabLayout.Tab?) {
        // Do nothing
    }
}
