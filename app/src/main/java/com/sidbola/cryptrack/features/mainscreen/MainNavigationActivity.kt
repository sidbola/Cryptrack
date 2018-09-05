package com.sidbola.cryptrack.features.mainscreen

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.sidbola.cryptrack.R
import com.sidbola.cryptrack.database.Transaction
import com.sidbola.cryptrack.features.authentication.login.PinLoginActivity
import com.sidbola.cryptrack.features.coindetails.CoinInfoPageFragment
import com.sidbola.cryptrack.features.coindetails.SnapshotFragment
import com.sidbola.cryptrack.features.coindetails.holdings.HoldingsFragment
import com.sidbola.cryptrack.features.coindetails.holdings.TransactionEntryFragment
import com.sidbola.cryptrack.features.coindetails.news.NewsFragment
import com.sidbola.cryptrack.features.mainscreen.discover.CoinSearchFragment
import com.sidbola.cryptrack.features.mainscreen.discover.DiscoverFragment
import com.sidbola.cryptrack.features.mainscreen.portfolio.PortfolioFragment
import com.sidbola.cryptrack.features.mainscreen.watchlist.WatchlistFragment
import com.sidbola.cryptrack.features.shared.extensions.toast
import com.sidbola.cryptrack.features.shared.model.Article
import com.sidbola.cryptrack.features.shared.model.CoinData
import com.sidbola.cryptrack.features.shared.model.DiscoverData
import kotlinx.android.synthetic.main.activity_main_navigation.*

class MainNavigationActivity : AppCompatActivity(),
        DiscoverFragment.OnFragmentInteractionListener,
        PortfolioFragment.OnFragmentInteractionListener,
        WatchlistFragment.OnFragmentInteractionListener,
        CoinInfoPageFragment.OnFragmentInteractionListener,
        SnapshotFragment.OnFragmentInteractionListener,
        HoldingsFragment.OnFragmentInteractionListener,
        NewsFragment.OnFragmentInteractionListener,
        CoinSearchFragment.OnFragmentInteractionListener,
        TransactionEntryFragment.OnTransactionEntryInteractionListener {

    private var mSectionsPagerAdapter: TopLevelPageAdapter? = null
    private var mViewPager: ViewPager? = null
    private var appShowingSensitiveInfo = false

    private lateinit var tabLayout: TabLayout

    var discoverData: DiscoverData = DiscoverData()

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_navigation)

        mAuth = FirebaseAuth.getInstance()

        mAuth.signInAnonymously()
                .addOnCompleteListener(this) { signIn ->
                    if (signIn.isSuccessful) {

                        mDatabase = FirebaseDatabase.getInstance().reference.child("discoverData")

                        val coinListListener = object : ValueEventListener {
                            override fun onCancelled(p0: DatabaseError) {
                                toast("Listener Cancelled")
                                p0.message.let { toast(it) }
                            }

                            override fun onDataChange(p0: DataSnapshot) {
                                p0.child("top100").children.mapNotNullTo(discoverData.top100) {
                                    it.getValue<CoinData>(CoinData::class.java)
                                }

                                p0.child("topLosers").children.mapNotNullTo(discoverData.topLosers) {
                                    it.getValue<CoinData>(CoinData::class.java)
                                }

                                p0.child("topWinners").children.mapNotNullTo(discoverData.topWinners) {
                                    it.getValue<CoinData>(CoinData::class.java)
                                }

                                createPagerInterface()

                                setUiEnabled(true, mainFrame)
                                setUiEnabled(true, topLevelFragmentContainer)


                                val fragment = supportFragmentManager.findFragmentByTag("loading_data")
                                val fragmentTransaction = supportFragmentManager.beginTransaction()
                                fragmentTransaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                                fragmentTransaction.remove(fragment)
                                fragmentTransaction.commit()
                            }
                        }
                        mDatabase.addListenerForSingleValueEvent(coinListListener)

                        if (hasInternetConnection()) {
                            setUiEnabled(false, mainFrame)

                            val ft = supportFragmentManager.beginTransaction()
                            ft.setCustomAnimations(R.anim.fade_in, R.anim.fade_out)
                            ft.add(R.id.aquiringDataFragmentHolder, AcquiringDataFragment(), "loading_data")
                            ft.commit()
                        }
                    }
                }
    }

    fun createPagerInterface() {
        mSectionsPagerAdapter = TopLevelPageAdapter(supportFragmentManager)
        mViewPager = findViewById<ViewPager?>(R.id.topLevelFragmentContainer)
        mViewPager!!.offscreenPageLimit = 2
        mViewPager!!.adapter = mSectionsPagerAdapter

        tabLayout = findViewById<View>(R.id.tabs) as TabLayout

        val watchlistIcon = tabLayout.getTabAt(0)?.icon
        val portfolioIcon = tabLayout.getTabAt(1)?.icon
        val discoverIcon = tabLayout.getTabAt(2)?.icon

        tabLayout.setupWithViewPager(mViewPager)

        tabLayout.getTabAt(0)!!.icon = watchlistIcon
        tabLayout.getTabAt(1)!!.icon = portfolioIcon
        tabLayout.getTabAt(2)!!.icon = discoverIcon
        mViewPager?.setCurrentItem(2,true)
    }

    fun setUiEnabled(enabled_disabled: Boolean, viewGroup: ViewGroup) {
        var i = 0
        while (i < viewGroup.childCount) {
            val child: View = viewGroup.getChildAt(i)
            child.isEnabled = enabled_disabled
            if (child is ViewGroup) {
                setUiEnabled(enabled_disabled, child)
            }
            i++
        }
    }

    fun hasInternetConnection(): Boolean {
        val conManager: ConnectivityManager? =
                getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo: NetworkInfo? = conManager?.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    override fun onResume() {
        super.onResume()

        if (appShowingSensitiveInfo) {
            val intent = Intent(this, PinLoginActivity::class.java)
            startActivity(intent)
            appShowingSensitiveInfo = false
            finish()
        }
    }

    override fun onDiscoverFragmentInteraction(coin: CoinData) {

        val bundle = Bundle()
        bundle.putString("ticker", coin.ticker)
        bundle.putString("name", coin.name)
        bundle.putString("mc", coin.marketCapDisplay)
        bundle.putString("open", coin.open24hr)
        bundle.putString("low", coin.low24hr)
        bundle.putString("high", coin.high24hr)
        bundle.putString("volume", coin.volume24hr)
        bundle.putString("percentChange", coin.percentChange)
        bundle.putString("price", coin.price)

        val infoPage = CoinInfoPageFragment()
        infoPage.arguments = bundle

        setUiEnabled(false, topLevelFragmentContainer)

        val ft = supportFragmentManager.beginTransaction()
        ft.setCustomAnimations(R.anim.fade_in,
                R.anim.fade_out,
                R.anim.fade_in,
                R.anim.fade_out)
        ft.addToBackStack(null)
        ft.replace(R.id.coinInfoFragmentHolder, infoPage)
        ft.commit()
    }

    override fun onWatchlistFragmentInteraction(coin: CoinData) {
        val bundle = Bundle()
        bundle.putString("ticker", coin.ticker)
        bundle.putString("name", coin.name)
        bundle.putString("mc", coin.marketCapDisplay)
        bundle.putString("open", coin.open24hr)
        bundle.putString("low", coin.low24hr)
        bundle.putString("high", coin.high24hr)
        bundle.putString("volume", coin.volume24hr)
        bundle.putString("percentChange", coin.percentChange)
        bundle.putString("price", coin.price)

        val infoPage = CoinInfoPageFragment()
        infoPage.arguments = bundle

        setUiEnabled(false, topLevelFragmentContainer)

        val ft = supportFragmentManager.beginTransaction()
        ft.setCustomAnimations(R.anim.fade_in,
                R.anim.fade_out,
                R.anim.fade_in,
                R.anim.fade_out)
        ft.addToBackStack(null)
        ft.replace(R.id.coinInfoFragmentHolder, infoPage)
        ft.commit()
    }

    override fun onPortfolioFragmentInteraction(coinToDisplay: CoinData) {
        val bundle = Bundle()
        bundle.putString("ticker", coinToDisplay.ticker)
        bundle.putString("name", coinToDisplay.name)
        bundle.putString("mc", coinToDisplay.marketCapDisplay)
        bundle.putString("open", coinToDisplay.open24hr)
        bundle.putString("low", coinToDisplay.low24hr)
        bundle.putString("high", coinToDisplay.high24hr)
        bundle.putString("volume", coinToDisplay.volume24hr)
        bundle.putString("percentChange", coinToDisplay.percentChange)
        bundle.putString("price", coinToDisplay.price)

        val infoPage = CoinInfoPageFragment()
        infoPage.arguments = bundle

        setUiEnabled(false, topLevelFragmentContainer)

        val ft = supportFragmentManager.beginTransaction()
        ft.setCustomAnimations(R.anim.fade_in,
                R.anim.fade_out,
                R.anim.fade_in,
                R.anim.fade_out)
        ft.addToBackStack(null)
        ft.replace(R.id.coinInfoFragmentHolder, infoPage)
        ft.commit()
    }

    override fun onCoinInfoPageFragmentInteraction(ticker: String, didAdd: Boolean) {
        val tag = "android:switcher:" + R.id.topLevelFragmentContainer + ":" + 0

        val watchlistFragment = supportFragmentManager.findFragmentByTag(tag) as WatchlistFragment

        watchlistFragment.listChanged(ticker, didAdd)
    }

    override fun onNewsFragmentInteraction(response: Article) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(response.url))
        startActivity(intent)
    }

    override fun onHoldingsFragmentInteraction(response: String) {
        val portfolioTag = "android:switcher:" + R.id.topLevelFragmentContainer + ":" + 1

        val portfolioFragment = supportFragmentManager.findFragmentByTag(portfolioTag)

        if (portfolioFragment is PortfolioFragment) {
            portfolioFragment.updateList()
        } else {
            this.toast("Could not update portfolio")
        }
    }

    override fun onCoinSnapshotFragmentInteraction(response: String) {
        TODO("not implemented") // To change body of created functions use File | Settings | File Templates.
    }

    override fun onCoinSearchFragmentInteraction(ticker: String, isSearching: Boolean) {

        if (isSearching){
            mDatabase = FirebaseDatabase.getInstance().reference.child("allCoins").child(ticker)

            val coinListListener = object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    toast("Listener Cancelled")
                }

                override fun onDataChange(p0: DataSnapshot) {
                    val mRaw: Any

                    mRaw = when {
                        p0.child("marketCapRaw").value is Long ->
                            (p0.child("marketCapRaw").value as Long).toFloat()

                        p0.child("marketCapRaw").value is Double ->
                            (p0.child("marketCapRaw").value as Double).toFloat()

                        p0.child("marketCapRaw").value is Float ->
                            p0.child("marketCapRaw").value as Float

                        else -> p0.child("marketCapRaw").value as Float
                    }

                    val coin = CoinData(
                        p0.child("high24hr").value as String,
                        p0.child("low24hr").value as String,
                        p0.child("marketCapDisplay").value as String,
                        mRaw,
                        p0.child("name").value as String,
                        p0.child("open24hr").value as String,
                        p0.child("percentChange").value as String,
                        p0.child("price").value as String,
                        p0.child("supply").value as String,
                        p0.child("ticker").value as String,
                        p0.child("volume24hr").value as String,
                        ArrayList()
                    )

                    val bundle = Bundle()
                    bundle.putString("ticker", coin.ticker)
                    bundle.putString("name", coin.name)
                    bundle.putString("mc", coin.marketCapDisplay)
                    bundle.putString("open", coin.open24hr)
                    bundle.putString("low", coin.low24hr)
                    bundle.putString("high", coin.high24hr)
                    bundle.putString("volume", coin.volume24hr)
                    bundle.putString("percentChange", coin.percentChange)
                    bundle.putString("price", coin.price)

                    val infoPage = CoinInfoPageFragment()
                    infoPage.arguments = bundle

                    setUiEnabled(false, topLevelFragmentContainer)

                    val ft = supportFragmentManager.beginTransaction()
                    ft.setCustomAnimations(R.anim.fade_in,
                        R.anim.fade_out,
                        R.anim.fade_in,
                        R.anim.fade_out)
                    ft.addToBackStack(null)
                    ft.replace(R.id.coinInfoFragmentHolder, infoPage)
                    ft.commit()
                }
            }
            mDatabase.addListenerForSingleValueEvent(coinListListener)
        } else {
            // Update watchlist
            val tag = "android:switcher:" + R.id.topLevelFragmentContainer + ":" + 0

            val watchlistFragment = supportFragmentManager.findFragmentByTag(tag) as WatchlistFragment

            watchlistFragment.listChanged(ticker, true)
        }

    }

    override fun onTransactionEntryFragmentInteraction(addedTransaction: Transaction) {
        val tag = "android:switcher:" + R.id.coinInfoViewPager + ":" + 1

        // TODO: Clean this up

        var holdingsFragment = supportFragmentManager.findFragmentByTag(tag)

        for (frag in supportFragmentManager.fragments) {
            if (frag is CoinInfoPageFragment) {
                holdingsFragment = frag.childFragmentManager.findFragmentByTag(tag)
            }
        }

        if (holdingsFragment is HoldingsFragment) {
            holdingsFragment.newTransactionEntered()
        } else {
            this.toast("Could not add transaction")
        }

        val portfolioTag = "android:switcher:" + R.id.topLevelFragmentContainer + ":" + 1

        val portfolioFragment = supportFragmentManager.findFragmentByTag(portfolioTag)

        if (portfolioFragment is PortfolioFragment) {
            portfolioFragment.updateList()
        } else {
            this.toast("Could not update portfolio")
        }
    }
}