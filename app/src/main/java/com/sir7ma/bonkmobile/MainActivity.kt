
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

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
            View.SYSTEM_UI_FLAG_FULLSCREEN or
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        )

        web = WebView(this)

        web.settings.javaScriptEnabled = true
        web.settings.domStorageEnabled = true
        web.settings.mediaPlaybackRequiresUserGesture = false
        web.settings.useWideViewPort = true
        web.settings.loadWithOverviewMode = true
        web.settings.setSupportMultipleWindows(false)

        web.settings.setSupportZoom(false)
        web.settings.builtInZoomControls = false
        web.settings.displayZoomControls = false

        web.isVerticalScrollBarEnabled = false
        web.isHorizontalScrollBarEnabled = false
        web.overScrollMode = View.OVER_SCROLL_NEVER

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
        if (::web.isInitialized && web.canGoBack()) {
            web.goBack()
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        if (::web.isInitialized) web.destroy()
        super.onDestroy()
    }

    private val MOBILE_CONTROLS = """
    (function() {
      if (window.__bonkMobileInstalled) return;
      window.__bonkMobileInstalled = true;

      // -----------------------------------------
      // 1. FULL SCREEN OUTER WEBSITE
      // -----------------------------------------

      const outerStyle = document.createElement('style');

      outerStyle.textContent = `
        html, body {
          width: 100% !important;
          height: 100% !important;
          margin: 0 !important;
          padding: 0 !important;
          overflow: hidden !important;
          position: fixed !important;
          inset: 0 !important;
          background: #000 !important;
          overscroll-behavior: none !important;
        }

        #maingameframe {
          position: fixed !important;
          left: 0 !important;
          top: 0 !important;
          width: 100vw !important;
          height: 100vh !important;
          min-width: 100vw !important;
          min-height: 100vh !important;
          max-width: none !important;
          max-height: none !important;
          margin: 0 !important;
          padding: 0 !important;
          border: 0 !important;
          display: block !important;
          overflow: hidden !important;
          z-index: 1 !important;
        }

        body > *:not(#maingameframe):not(#bm-controls) {
          display: none !important;
        }

        #bm-controls {
          position: fixed !important;
          inset: 0 !important;
          width: 100vw !important;
          height: 100vh !important;
          z-index: 2147483647 !important;
          pointer-events: none;
          font-family: Arial, sans-serif;
          touch-action: none;
        }
      `;

      document.head.appendChild(outerStyle);

      // -----------------------------------------
      // 2. RESIZE THE GAME INSIDE THE IFRAME
      // -----------------------------------------

      function installGameLayout() {
        const frame = document.getElementById('maingameframe');

        if (!frame) return false;

        try {
          const d = frame.contentDocument;

          if (!d || !d.getElementById('newbonkgamecontainer')) {
            return false;
          }

          const game = d.getElementById('newbonkgamecontainer');
          const bonk = d.getElementById('bonkiocontainer');
          const page = d.getElementById('pagecontainer');

          if (!bonk || !page) return false;

          // Save the original logical game dimensions once.
          if (!game.dataset.bmBaseWidth) {
            game.dataset.bmBaseWidth = game.offsetWidth || 340;
            game.dataset.bmBaseHeight = game.offsetHeight || 233;
          }

          const baseWidth = Number(game.dataset.bmBaseWidth);
          const baseHeight = Number(game.dataset.bmBaseHeight);

          function resizeGame() {
            const width = d.documentElement.clientWidth ||
                          frame.clientWidth ||
                          window.innerWidth;

            const height = d.documentElement.clientHeight ||
                           frame.clientHeight ||
                           window.innerHeight;

            if (!width || !height || !baseWidth || !baseHeight) return;

            const scaleX = width / baseWidth;
            const scaleY = height / baseHeight;

            // Outer game page
            d.documentElement.style.setProperty(
              'width', '100%', 'important'
            );

            d.documentElement.style.setProperty(
              'height', '100%', 'important'
            );

            d.documentElement.style.setProperty(
              'overflow', 'hidden', 'important'
            );

            d.body.style.setProperty(
              'width', '100%', 'important'
            );

            d.body.style.setProperty(
              'height', '100%', 'important'
            );

            d.body.style.setProperty(
              'margin', '0', 'important'
            );

            d.body.style.setProperty(
              'overflow', 'hidden', 'important'
            );

            // Main page container
            page.style.setProperty('position', 'fixed', 'important');
            page.style.setProperty('left', '0', 'important');
            page.style.setProperty('top', '0', 'important');
            page.style.setProperty('width', width + 'px', 'important');
            page.style.setProperty('height', height + 'px', 'important');
            page.style.setProperty('display', 'block', 'important');
            page.style.setProperty('overflow', 'hidden', 'important');

            // Bonk container
            bonk.style.setProperty('position', 'fixed', 'important');
            bonk.style.setProperty('left', '0', 'important');
            bonk.style.setProperty('top', '0', 'important');
            bonk.style.setProperty('width', width + 'px', 'important');
            bonk.style.setProperty('height', height + 'px', 'important');
            bonk.style.setProperty('margin', '0', 'important');
            bonk.style.setProperty('overflow', 'hidden', 'important');

            // Scale the entire game interface, not only the renderer.
            game.style.setProperty('position', 'absolute', 'important');
            game.style.setProperty('left', '0', 'important');
            game.style.setProperty('top', '0', 'important');
            game.style.setProperty('width', baseWidth + 'px', 'important');
            game.style.setProperty('height', baseHeight + 'px', 'important');
            game.style.setProperty('margin', '0', 'important');
            game.style.setProperty('transform-origin', '0 0', 'important');

            game.style.setProperty(
              'transform',
              'scale(' + scaleX + ',' + scaleY + ')',
              'important'
            );
          }

          if (!d.getElementById('bm-game-style')) {
            const style = d.createElement('style');
            style.id = 'bm-game-style';

            style.textContent = `
              html, body {
                margin: 0 !important;
                padding: 0 !important;
                overflow: hidden !important;
                overscroll-behavior: none !important;
              }

              #pagecontainer {
                overflow: hidden !important;
              }

              #bonkiocontainer {
                overflow: hidden !important;
              }
            `;

            d.head.appendChild(style);
          }

          resizeGame();

          if (!d.__bmResizeInstalled) {
            d.__bmResizeInstalled = true;
            d.defaultView.addEventListener('resize', resizeGame);
          }

          return true;

        } catch (e) {
          console.log('Bonk layout error:', e);
          return false;
        }
      }

      // Wait for the game iframe to finish loading.
      const frame = document.getElementById('maingameframe');

      if (frame) {
        frame.addEventListener('load', installGameLayout);
      }

      let attempts = 0;

      const layoutTimer = setInterval(function() {
        attempts++;

        if (installGameLayout() || attempts >= 40) {
          clearInterval(layoutTimer);
        }
      }, 500);

      // -----------------------------------------
      // 3. MOBILE CONTROL BUTTONS
      // -----------------------------------------

      const controlStyle = document.createElement('style');

      controlStyle.textContent = `
        .bm-btn {
          pointer-events: auto;
          user-select: none;
          -webkit-user-select: none;
          touch-action: none;
          display: flex;
          align-items: center;
          justify-content: center;
          color: white;
          font-weight: 800;
          background: rgba(25,25,30,.38);
          border: 2px solid rgba(255,255,255,.55);
          box-shadow: 0 3px 12px #0005;
          text-shadow: 0 1px 3px #000;
          border-radius: 50%;
        }

        #bm-stick {
          position: absolute;
          left: 3.5vw;
          bottom: 5vh;
          width: min(24vw,150px);
          height: min(24vw,150px);
          border-radius: 50%;
          background: rgba(255,255,255,.10);
          border: 2px solid rgba(255,255,255,.35);
          pointer-events: auto;
          touch-action: none;
        }

        #bm-knob {
          position: absolute;
          width: 42%;
          height: 42%;
          left: 29%;
          top: 29%;
          border-radius: 50%;
          background: rgba(255,255,255,.48);
          border: 2px solid #fff9;
          pointer-events: none;
        }

        #bm-heavy {
          position: absolute;
          right: 5vw;
          bottom: 9vh;
          width: min(18vw,112px);
          height: min(18vw,112px);
          font-size: clamp(24px,5vw,42px);
        }

        #bm-special {
          position: absolute;
          right: 20vw;
          bottom: 7vh;
          width: min(13vw,82px);
          height: min(13vw,82px);
          font-size: clamp(18px,4vw,30px);
        }

        #bm-hint {
          position: absolute;
          left: 50%;
          bottom: 2vh;
          transform: translateX(-50%);
          color: #fff9;
          font-size: 12px;
          text-shadow: 0 1px 4px #000;
        }
      `;

      document.head.appendChild(controlStyle);

      const root = document.createElement('div');
      root.id = 'bm-controls';

      root.innerHTML =
        '<div id="bm-stick"><div id="bm-knob"></div></div>' +
        '<div id="bm-heavy" class="bm-btn">X</div>' +
        '<div id="bm-special" class="bm-btn">Z</div>' +
        '<div id="bm-hint">اسحب العصا للحركة • اضغط X للثقل</div>';

      document.body.appendChild(root);

      // -----------------------------------------
      // 4. SEND KEY EVENTS TO THE GAME IFRAME
      // -----------------------------------------

      const findGameDoc = () => {
        try {
          const f = document.getElementById('maingameframe');
          if (f && f.contentDocument) return f.contentDocument;
        } catch (e) {}

        return document;
      };

      const key = (code, down) => {
        const d = findGameDoc();
        const target = d.activeElement || d.body || d;

        const names = {
          37: 'ArrowLeft',
          38: 'ArrowUp',
          39: 'ArrowRight',
          40: 'ArrowDown'
        };

        const keyName = code === 88 ? 'x' :
                        code === 90 ? 'z' :
                        (names[code] || '');

        const keyCodeName = code === 88 ? 'KeyX' :
                            code === 90 ? 'KeyZ' :
                            (names[code] || '');

        const event = new KeyboardEvent(
          down ? 'keydown' : 'keyup',
          {
            key: keyName,
            code: keyCodeName,
            keyCode: code,
            which: code,
            bubbles: true,
            cancelable: true
          }
        );

        try {
          target.dispatchEvent(event);
          d.dispatchEvent(event);
          d.defaultView.dispatchEvent(event);
        } catch (e) {}
      };

      // -----------------------------------------
      // 5. JOYSTICK
      // -----------------------------------------

      const stick = document.getElementById('bm-stick');
      const knob = document.getElementById('bm-knob');

      let active = false;
      let held = {};

      function set(code, on) {
        if (held[code] !== on) {
          held[code] = on;
          key(code, on);
        }
      }

      function release() {
        [37, 38, 39, 40].forEach(c => set(c, false));
        knob.style.transform = 'translate(0,0)';
        active = false;
      }

      function move(e) {
        const r = stick.getBoundingClientRect();

        const cx = r.left + r.width / 2;
        const cy = r.top + r.height / 2;

        let dx = e.clientX - cx;
        let dy = e.clientY - cy;

        const max = r.width * .31;
        const len = Math.hypot(dx, dy) || 1;

        if (len > max) {
          dx = dx / len * max;
          dy = dy / len * max;
        }

        knob.style.transform =
          'translate(' + dx + 'px,' + dy + 'px)';

        set(37, dx < -max * .25);
        set(39, dx > max * .25);
        set(38, dy < -max * .25);
        set(40, dy > max * .25);
      }

      stick.addEventListener('pointerdown', e => {
        active = true;
        stick.setPointerCapture(e.pointerId);
        move(e);
        e.preventDefault();
      });

      stick.addEventListener('pointermove', e => {
        if (active) move(e);
      });

      stick.addEventListener('pointerup', release);
      stick.addEventListener('pointercancel', release);

      // -----------------------------------------
      // 6. X AND Z BUTTONS
      // -----------------------------------------

      const hold = (id, code) => {
        const el = document.getElementById(id);

        el.addEventListener('pointerdown', e => {
          el.setPointerCapture(e.pointerId);
          set(code, true);
          e.preventDefault();
        });

        ['pointerup', 'pointercancel', 'lostpointercapture'].forEach(t => {
          el.addEventListener(t, () => set(code, false));
        });
      };

      hold('bm-heavy', 88);
      hold('bm-special', 90);

      document.addEventListener('visibilitychange', () => {
        if (document.hidden) {
          release();
          set(88, false);
          set(90, false);
        }
      });

    })();
    """.trimIndent()
}
