package app.olauncher.aham

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    private val twitterUrl = "https://mobile.twitter.com/"
    private val fragmentManager = supportFragmentManager

    private lateinit var homeFragment: MainFragment
    private lateinit var searchFragment: MainFragment
    private lateinit var notificationsFragment: MainFragment
    private lateinit var messagesFragment: MainFragment
    private lateinit var activeFragment: Fragment

    override fun onBackPressed() {
        when (activeFragment) {
            homeFragment -> {
                if (homeFragment.theWebView.canGoBack())
                    homeFragment.theWebView.goBack()
                else super.onBackPressed()
            }
            searchFragment -> {
                if (searchFragment.theWebView.canGoBack())
                    searchFragment.theWebView.goBack()
                else super.onBackPressed()
            }
            notificationsFragment -> {
                if (notificationsFragment.theWebView.canGoBack())
                    notificationsFragment.theWebView.goBack()
                else super.onBackPressed()
            }
            messagesFragment -> {
                if (messagesFragment.theWebView.canGoBack())
                    messagesFragment.theWebView.goBack()
                else super.onBackPressed()
            }
            else -> super.onBackPressed()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initFragment()
        addFragments()
        setBottomNavListener()
        next.setOnClickListener { saveUser() }

        fragmentManager.beginTransaction().hide(activeFragment).show(homeFragment).commit()
        activeFragment = homeFragment

        if (Prefs(this).username.isEmpty())
            userNameLayout.visibility = View.VISIBLE
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

        activeFragment = homeFragment
    }

    private fun addFragments() {
        fragmentManager.beginTransaction().apply {
            add(R.id.container, homeFragment, getString(R.string.title_home)).hide(homeFragment)
            add(R.id.container, searchFragment, getString(R.string.title_search)).hide(searchFragment)
            add(R.id.container, notificationsFragment, getString(R.string.title_notifications)).hide(notificationsFragment)
            add(R.id.container, messagesFragment, getString(R.string.title_messages)).hide(messagesFragment)
        }.commit()
    }

    private fun setBottomNavListener() {
        bottomNavView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navigation_home -> {
                    if (activeFragment == homeFragment) homeFragment.loadUrl()
                    else {
                        fragmentManager.beginTransaction().hide(activeFragment).show(homeFragment).commit()
                        activeFragment = homeFragment
                    }
                    true
                }
                R.id.navigation_search -> {
                    if (activeFragment == searchFragment) searchFragment.loadUrl()
                    else {
                        fragmentManager.beginTransaction().hide(activeFragment).show(searchFragment).commit()
                        activeFragment = searchFragment
                    }
                    true
                }
                R.id.navigation_notifications -> {
                    if (activeFragment == notificationsFragment) notificationsFragment.loadUrl()
                    else {
                        fragmentManager.beginTransaction().hide(activeFragment).show(notificationsFragment).commit()
                        activeFragment = notificationsFragment
                    }
                    true
                }
                R.id.navigation_messages -> {
                    if (activeFragment == messagesFragment) messagesFragment.loadUrl()
                    else {
                        fragmentManager.beginTransaction().hide(activeFragment).show(messagesFragment).commit()
                        activeFragment = messagesFragment
                    }
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
                bottomNavView.visibility = View.VISIBLE
                divider.visibility = View.VISIBLE

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
                            divider.visibility = View.GONE
                        }
                    }
                    else -> super.onReceivedTitle(webView, title)
                }
            }
        }
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
}