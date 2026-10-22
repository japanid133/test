package com.example.coinminer

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlin.math.ceil
import kotlin.math.pow

// ======================= KONFIGURASI =======================
// GANTI dengan Rewarded Ad Unit ID milikmu. Nilai di bawah adalah ID TEST resmi Google.
// Saat development WAJIB pakai test ad dan jangan klik iklan asli milikmu sendiri.
private const val REWARDED_AD_UNIT_ID = "ca-app-pub-9666607144175992/4280481113"

private const val REWARD_COINS = 100        // koin per satu iklan yang selesai ditonton
private const val COOLDOWN_MS = 10_000L     // jeda sebelum tombol iklan aktif lagi (10 detik)
// ===========================================================

// Perlengkapan mining: menambah koin pasif per detik selama aplikasi dibuka
private data class Gear(val name: String, val baseCost: Double, val ratePerLevel: Double)

private val GEARS = listOf(
    Gear("Beliung Kayu", 200.0, 0.2),
    Gear("Helm Tambang", 1_000.0, 1.0),
    Gear("Troli Tambang", 5_000.0, 5.0),
    Gear("Bor Listrik", 25_000.0, 25.0),
)

class MainActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences
    private var coins = 0.0
    private val levels = IntArray(GEARS.size)

    private var rewardedAd: RewardedAd? = null
    private var isLoadingAd = false
    private var cooldownEndsAt = 0L

    private val handler = Handler(Looper.getMainLooper())
    private var tickCount = 0

    private lateinit var coinText: TextView
    private lateinit var rateText: TextView
    private lateinit var adButton: Button
    private val gearInfo = mutableListOf<TextView>()
    private val gearButton = mutableListOf<Button>()

    private val ticker = object : Runnable {
        override fun run() {
            coins += passiveRate()
            if (++tickCount % 5 == 0) save()
            refreshUi()
            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = getSharedPreferences("miner", Context.MODE_PRIVATE)
        coins = prefs.getFloat("coins", 0f).toDouble()
        for (i in GEARS.indices) levels[i] = prefs.getInt("gear_$i", 0)

        buildUi()
        MobileAds.initialize(this) {}
        loadAd()
    }

    override fun onResume() {
        super.onResume()
        handler.post(ticker)
    }

    override fun onPause() {
        handler.removeCallbacks(ticker)
        save()
        super.onPause()
    }

    // ---------------- Logika game ----------------

    private fun passiveRate(): Double =
        GEARS.indices.sumOf { GEARS[it].ratePerLevel * levels[it] }

    private fun gearCost(i: Int): Long =
        (GEARS[i].baseCost * 1.6.pow(levels[i])).toLong()

    private fun buyGear(i: Int) {
        val cost = gearCost(i)
        if (coins >= cost) {
            coins -= cost
            levels[i]++
            save()
            refreshUi()
        } else {
            Toast.makeText(this, "Koin belum cukup.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun save() {
        prefs.edit().apply {
            putFloat("coins", coins.toFloat())
            for (i in GEARS.indices) putInt("gear_$i", levels[i])
        }.apply()
    }

    // ---------------- Iklan rewarded (dimulai oleh pengguna) ----------------

    private fun loadAd() {
        if (isLoadingAd || rewardedAd != null) return
        isLoadingAd = true
        RewardedAd.load(this, REWARDED_AD_UNIT_ID, AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    isLoadingAd = false
                    refreshUi()
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedAd = null
                    isLoadingAd = false
                    // coba lagi beberapa detik kemudian
                    handler.postDelayed({ loadAd() }, 15_000)
                    refreshUi()
                }
            })
    }

    private fun onWatchAdClicked() {
        if (System.currentTimeMillis() < cooldownEndsAt) return
        val ad = rewardedAd ?: run { loadAd(); return }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                rewardedAd = null
                cooldownEndsAt = System.currentTimeMillis() + COOLDOWN_MS
                loadAd()
                refreshUi()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                rewardedAd = null
                loadAd()
                refreshUi()
            }
        }

        // Koin hanya diberikan jika iklan ditonton sampai syarat reward terpenuhi
        ad.show(this) {
            coins += REWARD_COINS
            save()
            Toast.makeText(this, "+$REWARD_COINS koin!", Toast.LENGTH_SHORT).show()
            refreshUi()
        }
    }

    // ---------------- UI ----------------

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    private fun buildUi() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(32), dp(20), dp(20))
        }

        TextView(this).apply {
            text = "⛏️ Coin Miner"
            textSize = 26f
            gravity = Gravity.CENTER
            root.addView(this, matchWrap())
        }

        coinText = TextView(this).apply {
            textSize = 34f
            gravity = Gravity.CENTER
            setPadding(0, dp(16), 0, 0)
            root.addView(this, matchWrap())
        }

        rateText = TextView(this).apply {
            textSize = 14f
            gravity = Gravity.CENTER
            setTextColor(Color.GRAY)
            root.addView(this, matchWrap())
        }

        adButton = Button(this).apply {
            textSize = 16f
            setOnClickListener { onWatchAdClicked() }
            root.addView(this, matchWrap().apply { topMargin = dp(20); bottomMargin = dp(20) })
        }

        TextView(this).apply {
            text = "Upgrade Perlengkapan"
            textSize = 18f
            root.addView(this, matchWrap())
        }

        GEARS.forEachIndexed { i, _ ->
            val info = TextView(this).apply {
                textSize = 14f
                setPadding(0, dp(12), 0, 0)
                root.addView(this, matchWrap())
            }
            val btn = Button(this).apply {
                setOnClickListener { buyGear(i) }
                root.addView(this, matchWrap())
            }
            gearInfo.add(info)
            gearButton.add(btn)
        }

        setContentView(ScrollView(this).apply { addView(root) })
    }

    private fun matchWrap() = LinearLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
    )

    private fun refreshUi() {
        coinText.text = "🪙 ${coins.toLong()}"
        rateText.text = "Mining pasif: ${"%.1f".format(passiveRate())} koin/detik (saat aplikasi dibuka)"

        val remainMs = cooldownEndsAt - System.currentTimeMillis()
        when {
            remainMs > 0 -> {
                adButton.isEnabled = false
                adButton.text = "Tunggu ${ceil(remainMs / 1000.0).toInt()} detik…"
            }
            rewardedAd == null -> {
                adButton.isEnabled = false
                adButton.text = "Memuat iklan…"
            }
            else -> {
                adButton.isEnabled = true
                adButton.text = "▶ Tonton iklan (+$REWARD_COINS koin)"
            }
        }

        GEARS.forEachIndexed { i, g ->
            gearInfo[i].text = "${g.name}  (Lv ${levels[i]})  •  +${g.ratePerLevel} koin/detik per level"
            val cost = gearCost(i)
            gearButton[i].text = "Upgrade — $cost koin"
            gearButton[i].isEnabled = coins >= cost
        }
    }
}
