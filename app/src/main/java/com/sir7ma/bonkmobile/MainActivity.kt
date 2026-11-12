package com.sir7ma.bonkmobile

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.content.pm.ActivityInfo

class MainActivity : Activity() {
    private lateinit var web: WebView

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_FULLSCREEN or
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        )
        web = WebView(this)
        web.settings.javaScriptEnabled = true
        web.settings.domStorageEnabled = true
        web.settings.mediaPlaybackRequiresUserGesture = false
        web.settings.useWideViewPort = true
        web.settings.loadWithOverviewMode = true
        web.settings.setSupportMultipleWindows(false)
        web.webChromeClient = WebChromeClient()
        web.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                view.evaluateJavascript(MOBILE_CONTROLS, null)
            }
        }
        setContentView(web)
        web.loadUrl("https://bonk.io/")
    }

    override fun onBackPressed() {
        if (::web.isInitialized && web.canGoBack()) web.goBack() else super.onBackPressed()
    }

    override fun onDestroy() {
        if (::web.isInitialized) web.destroy()
        super.onDestroy()
    }

    private val MOBILE_CONTROLS = """
    (function(){
      if (window.__bonkTouchInstalled) return;
      window.__bonkTouchInstalled = true;
      const css = `#bm-controls{position:fixed;inset:0;z-index:2147483647;pointer-events:none;font-family:Arial,sans-serif;touch-action:none}
      .bm-btn{pointer-events:auto;user-select:none;-webkit-user-select:none;touch-action:none;display:flex;align-items:center;justify-content:center;color:white;font-weight:800;background:rgba(25,25,30,.38);border:2px solid rgba(255,255,255,.55);box-shadow:0 3px 12px #0005;text-shadow:0 1px 3px #000;border-radius:50%;}
      #bm-stick{position:absolute;left:3.5vw;bottom:5vh;width:min(24vw,150px);height:min(24vw,150px);border-radius:50%;background:rgba(255,255,255,.10);border:2px solid rgba(255,255,255,.35);pointer-events:auto;touch-action:none}
      #bm-knob{position:absolute;width:42%;height:42%;left:29%;top:29%;border-radius:50%;background:rgba(255,255,255,.48);border:2px solid #fff9;pointer-events:none}
      #bm-heavy{position:absolute;right:5vw;bottom:9vh;width:min(18vw,112px);height:min(18vw,112px);font-size:clamp(24px,5vw,42px)}
      #bm-special{position:absolute;right:20vw;bottom:7vh;width:min(13vw,82px);height:min(13vw,82px);font-size:clamp(18px,4vw,30px)}
      #bm-hint{position:absolute;left:50%;bottom:2vh;transform:translateX(-50%);color:#fff9;font-size:12px;text-shadow:0 1px 4px #000}`;
      const style=document.createElement('style'); style.textContent=css; document.head.appendChild(style);
      const root=document.createElement('div'); root.id='bm-controls';
      root.innerHTML='<div id="bm-stick"><div id="bm-knob"></div></div><div id="bm-heavy" class="bm-btn">X</div><div id="bm-special" class="bm-btn">Z</div><div id="bm-hint">اسحب العصا للحركة • اضغط X للثقل</div>';
      document.body.appendChild(root);
      const findGameDoc=()=>{ try { const frames=[...document.querySelectorAll('iframe')]; for(const f of frames){ try { if(f.contentDocument) { if(f.contentDocument.querySelector('#gamerenderer') || f.contentDocument.body) return f.contentDocument; } } catch(e){} } }catch(e){} return document; };
      const key=(code,down)=>{ const d=findGameDoc(); const target=d.activeElement||d; const keyName=code===88?'x':code===90?'z':({37:'ArrowLeft',38:'ArrowUp',39:'ArrowRight',40:'ArrowDown'}[code]||''); const ev=new KeyboardEvent(down?'keydown':'keyup',{key:keyName,code:code===88?'KeyX':code===90?'KeyZ':({37:'ArrowLeft',38:'ArrowUp',39:'ArrowRight',40:'ArrowDown'}[code]||''),keyCode:code,which:code,bubbles:true,cancelable:true}); try{target.dispatchEvent(ev);d.dispatchEvent(ev);window.dispatchEvent(ev)}catch(e){} };
      const stick=document.getElementById('bm-stick'), knob=document.getElementById('bm-knob'); let active=false, held={};
      function set(code,on){if(held[code]!==on){held[code]=on;key(code,on)}}
      function release(){[37,38,39,40].forEach(c=>set(c,false));knob.style.transform='translate(0,0)';active=false}
      function move(e){const r=stick.getBoundingClientRect(),cx=r.left+r.width/2,cy=r.top+r.height/2;let dx=e.clientX-cx,dy=e.clientY-cy;const max=r.width*.31,len=Math.hypot(dx,dy)||1;if(len>max){dx=dx/len*max;dy=dy/len*max}knob.style.transform='translate('+dx+'px,'+dy+'px)';set(37,dx < -max*.25);set(39,dx > max*.25);set(38,dy < -max*.25);set(40,dy > max*.25)}
      stick.addEventListener('pointerdown',e=>{active=true;stick.setPointerCapture(e.pointerId);move(e);e.preventDefault()});stick.addEventListener('pointermove',e=>{if(active)move(e)});stick.addEventListener('pointerup',release);stick.addEventListener('pointercancel',release);
      const hold=(id,code)=>{const el=document.getElementById(id);el.addEventListener('pointerdown',e=>{el.setPointerCapture(e.pointerId);set(code,true);e.preventDefault()});['pointerup','pointercancel','lostpointercapture'].forEach(t=>el.addEventListener(t,()=>set(code,false)))};
      hold('bm-heavy',88);hold('bm-special',90);
      document.addEventListener('visibilitychange',()=>{if(document.hidden){release();set(88,false);set(90,false)}});
    })();
    """.trimIndent()
}
