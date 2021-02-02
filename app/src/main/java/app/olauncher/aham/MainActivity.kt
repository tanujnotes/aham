package app.olauncher.aham

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.webkit.*
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_main.*
import java.net.URL
import java.util.*


class MainActivity : AppCompatActivity() {
    private val twitterUrl = "https://mobile.twitter.com/"
    private val fileChooserRequestCode = 10
    private val positionHome = 0
    private val positionSearch = 1
    private val positionNotification = 2
    private val positionMessage = 3

    private var pressedTime: Long = 0
    private var filePath: ValueCallback<Array<Uri>>? = null
    private var results = mutableListOf<Uri>()

    private lateinit var homeFragment: MainFragment
    private lateinit var searchFragment: MainFragment
    private lateinit var notificationsFragment: MainFragment
    private lateinit var messagesFragment: MainFragment

    private fun backAgainToExit() {
        if (pressedTime + 2000 > System.currentTimeMillis()) super.onBackPressed()
        else Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show()
        pressedTime = System.currentTimeMillis();
    }

    override fun onBackPressed() {
        when (viewPager.currentItem) {
            positionHome -> {
                if (homeFragment.theWebView.canGoBack())
                    homeFragment.theWebView.goBack()
                else backAgainToExit()
            }
            positionSearch -> {
                if (searchFragment.theWebView.canGoBack())
                    searchFragment.theWebView.goBack()
                else backAgainToExit()
            }
            positionNotification -> {
                if (notificationsFragment.theWebView.canGoBack())
                    notificationsFragment.theWebView.goBack()
                else backAgainToExit()
            }
            positionMessage -> {
                if (messagesFragment.theWebView.canGoBack())
                    messagesFragment.theWebView.goBack()
                else backAgainToExit()
            }
            else -> backAgainToExit()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initFragment()
        setBottomNavListener()
        next.setOnClickListener { saveUser() }

        val adapter = ViewPagerAdapter(supportFragmentManager)
        addFragments(adapter)
        viewPager.offscreenPageLimit = 3
        viewPager.adapter = adapter
        viewPager.setOnTouchListener { _, _ -> true }

        handleIntent(intent)

        if (Prefs(this).username.isEmpty())
            userNameLayout.visibility = View.VISIBLE
    }

    override fun onNewIntent(intent: Intent) {
        handleIntent(intent)
        super.onNewIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        if (Intent.ACTION_SEND == intent.action) {
            Toast.makeText(this, "Tap gallery icon to share", Toast.LENGTH_SHORT).show()
            getUriFromIntent(intent)
        } else {
            val uri = intent.data ?: return
            val url = URL(uri.scheme, uri.host, uri.path).toString()
            if (isRestrictedUrl(url)) return

            viewPager.setCurrentItem(positionHome, false)
            val handler = Handler(Looper.getMainLooper())
            handler.postDelayed(object : Runnable {
                override fun run() {
                    if (this@MainActivity::homeFragment.isInitialized)
                        homeFragment.loadUrl(url)
                    else handler.postDelayed(this, 1000)
                }
            }, 0)
        }
    }

    private fun saveUser() {
        if (instructions.visibility == View.VISIBLE) {
            finish()
            startActivity(intent)
        } else {
            val username = usernameInput.text.toString().trim()
            if (username.isEmpty()) return
            if (username.contains("@")) {
                Toast.makeText(this, "Enter username without @", Toast.LENGTH_SHORT).show()
                return
            }
            // Save the username
            Prefs(this).username = username
            usernameInput.visibility = View.GONE
            instructions.visibility = View.VISIBLE
        }
    }

    private fun isRestrictedUrl(url: String): Boolean {
        if (url.isEmpty()
            || url.endsWith("twitter.com")
            || url.endsWith("twitter.com/")
            || url.contains("twitter.com/home")
            || url.contains("twitter.com/explore")
        ) return true
        return false
    }

    private fun initFragment() {
        homeFragment = MainFragment(
            twitterUrl + Prefs(this).username,
            getWebChromeClient(),
            getWebViewClient()
        )
        searchFragment = MainFragment(
            twitterUrl + "search?q=%23aham&f=live",
            getWebChromeClient(),
            getWebViewClient()
        )
        notificationsFragment = MainFragment(
            twitterUrl + "notifications",
            getWebChromeClient(),
            getWebViewClient()
        )
        messagesFragment = MainFragment(
            twitterUrl + "messages/",
            getWebChromeClient(),
            getWebViewClient()
        )
    }

    private fun addFragments(adapter: ViewPagerAdapter) {
        adapter.addFragment(homeFragment, "Home")
        adapter.addFragment(searchFragment, "Search")
        adapter.addFragment(notificationsFragment, "Notifications")
        adapter.addFragment(messagesFragment, "Messages")
    }

    private fun setBottomNavListener() {
        bottomNavView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navigation_home -> {
                    if (viewPager.currentItem == positionHome) homeFragment.loadUrl()
                    else viewPager.setCurrentItem(positionHome, false)
                    true
                }
                R.id.navigation_search -> {
                    if (viewPager.currentItem == positionSearch) searchFragment.loadUrl()
                    else viewPager.setCurrentItem(positionSearch, false)
                    true
                }
                R.id.navigation_notifications -> {
                    if (viewPager.currentItem == positionNotification) notificationsFragment.loadUrl()
                    else viewPager.setCurrentItem(positionNotification, false)
                    true
                }
                R.id.navigation_messages -> {
                    if (viewPager.currentItem == positionMessage) messagesFragment.loadUrl()
                    else viewPager.setCurrentItem(positionMessage, false)
                    true
                }
                else -> false
            }
        }
    }

    private fun getWebChromeClient(): WebChromeClient {
        return object : WebChromeClient() {
            override fun onReceivedTitle(webView: WebView, title: String?) {
                val url: String = webView.url.toString()
                Log.d("URL:", url)
                bottomNavView.visibility = View.VISIBLE

                when {
                    url.contains("twitter.com/home") or url.contains("twitter.com/explore") -> {
                        webView.goBack()
                    }
                    url.contains("twitter.com/messages") -> {
                        val split = url.split("twitter.com/messages/")
                        if (split.size != 2)
                            super.onReceivedTitle(webView, title)
                        else if (split[1].contains("-") or (split[1] == "compose")) {
                            bottomNavView.visibility = View.GONE
                        }
                    }
                    url.contains("twitter.com/compose")
                            or url.contains("twitter.com/i/display")
                            or url.contains("twitter.com/i/newsletters")
                            or url.contains("twitter.com/settings/profile")
                            or url.contains("twitter.com/account/add")
                            or url.contains("twitter.com/account/switch")
                            or url.contains("twitter.com/account/begin_password_reset")
                            or url.contains("twitter.com/account/verify_user_info")
                            or url.contains("twitter.com/" + Prefs(this@MainActivity).username + "/photo")
                            or url.contains("twitter.com/" + Prefs(this@MainActivity).username + "/header_photo")
                            or url.contains("twitter.com/?logout")
                            or url.contains("ads.twitter.com")
                            or url.contains("help.twitter.com/")
                            or url.contains("analytics.twitter.com")
                            or (url.contains("twitter.com")
                            and url.contains("status")
                            and url.contains("photo/")) -> {
                        bottomNavView.visibility = View.GONE
                    }
                }
                super.onReceivedTitle(webView, title)
            }

            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                filePath = filePathCallback
                if (results.isNotEmpty()) {
                    filePath!!.onReceiveValue(results.toTypedArray())
                    filePath = null
                    results.clear()
                } else
                    openFileChooserActivity()
                return true
            }
        }
    }

    private fun openFileChooserActivity() {
        val photoLibraryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        photoLibraryIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        photoLibraryIntent.type = "image/* video/*"
        startActivityForResult(Intent.createChooser(photoLibraryIntent, "Please select"), fileChooserRequestCode)
    }

    private fun getWebViewClient(): WebViewClient {
        return object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                return if (request.url.host?.contains("twitter.com") == true)
                    false
                else {
                    openUrl(request.url.toString())
                    true
                }
            }
        }
    }

    private fun getSearchHashtag(): String {
        return when (Calendar.getInstance().get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> "%23MondayMotivation"
            Calendar.TUESDAY -> "%23TuesdayThoughts"
            Calendar.WEDNESDAY -> "%23WednesdayWisdom"
            Calendar.THURSDAY -> "%23ThursdayThoughts"
            Calendar.FRIDAY -> "%23FridayFeeling"
            Calendar.SATURDAY -> "%23WeekendVibes"
            Calendar.SUNDAY -> "%23SundayFunday"
            else -> "%23quoteoftheday"
        }
    }

    private fun openUrl(url: String?) {
        if (url.isNullOrEmpty()) return
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)
        startActivity(intent)
    }

    private fun getUriFromIntent(intent: Intent) {
        val dataString = intent.dataString
        val clipData = intent.clipData
        if (clipData != null) {
            if (clipData.itemCount > 4) {
                Toast.makeText(this, "Please choose up to 4 photos or 1 video", Toast.LENGTH_LONG).show()
                return
            }
            for (i in 0 until clipData.itemCount) {
                val item = clipData.getItemAt(i)
                results.add(item.uri)
            }
        }
        if (dataString != null) results = mutableListOf(Uri.parse(dataString))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (requestCode != fileChooserRequestCode || filePath == null) return
        if (resultCode == RESULT_OK) {
            intent?.let { getUriFromIntent(it) }
        }
        filePath!!.onReceiveValue(results.toTypedArray())
        filePath = null
        results.clear()
    }

    class ViewPagerAdapter(supportFragmentManager: FragmentManager) : FragmentPagerAdapter(supportFragmentManager) {

        // objects of arraylist. One is of Fragment type and
        // another one is of String type.*/
        private var fragmentList1: ArrayList<Fragment> = ArrayList()
        private var fragmentTitleList1: ArrayList<String> = ArrayList()

        // returns which item is selected from arraylist of fragments.
        override fun getItem(position: Int): Fragment {
            return fragmentList1.get(position)
        }

        // returns which item is selected from arraylist of titles.
        @Nullable
        override fun getPageTitle(position: Int): CharSequence {
            return fragmentTitleList1.get(position)
        }

        // returns the number of items present in arraylist.
        override fun getCount(): Int {
            return fragmentList1.size
        }

        // this function adds the fragment and title in 2 separate  arraylist.
        fun addFragment(fragment: Fragment, title: String) {
            fragmentList1.add(fragment)
            fragmentTitleList1.add(title)
        }
    }
}