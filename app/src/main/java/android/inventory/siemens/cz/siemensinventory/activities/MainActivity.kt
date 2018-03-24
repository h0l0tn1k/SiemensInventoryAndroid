package android.inventory.siemens.cz.siemensinventory.activities

import android.app.Activity
import android.content.Intent
import android.inventory.siemens.cz.siemensinventory.adapters.PermissionsAdapter
import android.inventory.siemens.cz.siemensinventory.R
import android.inventory.siemens.cz.siemensinventory.api.entity.LoginUserScd
import android.inventory.siemens.cz.siemensinventory.entity.Permission
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import android.widget.TextView
import com.google.gson.Gson
import kotlinx.android.synthetic.main.profile.*


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private val LOGIN_ACTIVITY_REQUEST_CODE = 0
    private var user : LoginUserScd? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        nav_view.setNavigationItemSelectedListener(this)

        if(user == null) {
            startLoginActivity()
        }

        profileChangePasswordBtn.setOnClickListener { startChangePasswordActivity() }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == LOGIN_ACTIVITY_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK && data != null) {
                this.user = Gson().fromJson(data.getStringExtra("user"), LoginUserScd::class.java)
            }
        }
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        setUserDetails()
        setMenuItemsVisibility(menu)

        return true
    }

    private fun startChangePasswordActivity() {
        startActivity(Intent(this, ChangePasswordActivity::class.java))
    }

    private fun startLoginActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivityForResult(intent, LOGIN_ACTIVITY_REQUEST_CODE)
    }

    private fun setMenuItemsVisibility(menu : Menu) {
        //TODO finish Menu Items visibility
        menuInflater.inflate(R.menu.activity_main_drawer, menu)

        //TODO use some ID's instead of numbers, reordering of menu items will change these numbers
        val activitiesSubMenu = menu.getItem(1).subMenu

        //BORROW
//        activitiesSubMenu.getItem(0).isVisible = false
        //Inventory
//        activitiesSubMenu.getItem(1).isVisible = (this.user?.flagBorrow == true)
//        menu.getItem(R.id.nav_edit_user_permissions).isVisible = (this.user?.flagAdmin == true)
//        menu.getItem(R.id.nav_electric_revision).isVisible = (this.user?.flagRevision == true)
//        menu.getItem(R.id.nav_calibration).isVisible = (this.user?.flagCalibration)

    }

    private fun setUserDetails() {
        val navEmail = findViewById<TextView>(R.id.nav_signed_in_email)
        navEmail.text = this.user?.email
        val navName = findViewById<TextView>(R.id.nav_signed_in_name)
        navName.text = this.user?.getFullName()

        val superiorUser = findViewById<TextView>(R.id.profile_superior_user_value)
        superiorUser.text = this.user?.superiorName

        val loggedIUser = findViewById<TextView>(R.id.profile_loggedin_user_value)
        loggedIUser.text = this.user?.getFullName()

        val permissions = listOf(
                Permission("Read-only", user?.flagRead),
                Permission("Edit", user?.flagWrite),
                Permission("Borrowing", user?.flagBorrow),
                Permission("Inventory-making", user?.flagInventory),
                Permission("Revision-making", user?.flagRevision),
                Permission("Admin", user?.flagAdmin)
        )

        permissionsView.adapter = PermissionsAdapter(this, permissions)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        val intent = when (item.itemId) {

            //Activities
            //R.id.nav_borrow -> Intent(this, BorrowDeviceActivity::class.java)
            //TODO add inventory
            R.id.nav_electric_revision -> Intent(this, ElectricRevisionActivity::class.java)
            R.id.nav_calibration -> Intent(this, CalibrationActivity::class.java)
            R.id.nav_user_permissions -> Intent(this, EditUserPermissionsActivity::class.java)

            //Views
            R.id.nav_suppliers -> Intent(this, SuppliersActivity::class.java)
            R.id.nav_departments -> Intent(this, DepartmentsActivity::class.java)
            R.id.nav_company_owners -> Intent(this, CompanyOwnersActivity::class.java)
            R.id.nav_project -> Intent(this, ProjectsActivity::class.java)

            //Others
            R.id.nav_settings -> Intent(this, SettingsActivity::class.java)
            R.id.nav_logout -> {
                user = null
                Intent(this, LoginActivity::class.java)
            }
            //TODO add About
            else -> null
        }
        if(intent != null) startActivity(intent)

        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }
}
