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

              /* Hide only the specific spectating notice. */
              #ingametextwarning_spectating {
                display: none !important;
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
      // 3. INVISIBLE TOUCH ZONES + FLOATING JOYSTICK
      // -----------------------------------------

      const controlStyle = document.createElement('style');
      controlStyle.textContent = `
        #bm-controls {
          position: fixed !important;
          inset: 0 !important;
          width: 100vw !important;
          height: 100vh !important;
          z-index: 2147483647 !important;
          pointer-events: none !important;
          touch-action: none !important;
          user-select: none !important;
          -webkit-user-select: none !important;
        }

        #bm-left-zone {
          position: absolute;
          left: 0;
          top: 0;
          width: 50%;
          height: 100%;
          pointer-events: none;
          touch-action: none;
        }

        .bm-right-zone {
          position: absolute;
          right: 0;
          width: 50%;
          pointer-events: none;
          touch-action: none;
          background: transparent;
        }

        #bm-z-zone {
          top: 0;
          height: 50%;
        }

        #bm-x-zone {
          bottom: 0;
          height: 50%;
        }

        #bm-stick-visual {
          position: absolute;
          width: 132px;
          height: 132px;
          margin-left: -66px;
          margin-top: -66px;
          border-radius: 50%;
          background: rgba(255,255,255,.10);
          border: 2px solid rgba(255,255,255,.36);
          box-sizing: border-box;
          pointer-events: none;
          display: none;
        }

        #bm-knob {
          position: absolute;
          width: 54px;
          height: 54px;
          left: 37px;
          top: 37px;
          border-radius: 50%;
          background: rgba(255,255,255,.48);
          border: 2px solid rgba(255,255,255,.85);
          box-sizing: border-box;
        }
      `;
      document.head.appendChild(controlStyle);

      const root = document.createElement('div');
      root.id = 'bm-controls';
      root.innerHTML =
        '<div id="bm-left-zone"></div>' +
        '<div id="bm-z-zone" class="bm-right-zone"></div>' +
        '<div id="bm-x-zone" class="bm-right-zone"></div>' +
        '<div id="bm-stick-visual"><div id="bm-knob"></div></div>';
      document.body.appendChild(root);

      const leftZone = document.getElementById('bm-left-zone');
      const xZone = document.getElementById('bm-x-zone');
      const zZone = document.getElementById('bm-z-zone');
      const stickVisual = document.getElementById('bm-stick-visual');
      const knob = document.getElementById('bm-knob');

      // -----------------------------------------
      // 4. SEND KEY EVENTS TO THE GAME IFRAME
      // -----------------------------------------

      const findGameDoc = () => {
        try {
          const frame = document.getElementById('maingameframe');
          if (frame && frame.contentDocument) return frame.contentDocument;
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

        const eventInit = {
          key: keyName,
          code: keyCodeName,
          keyCode: code,
          which: code,
          bubbles: true,
          cancelable: true
        };

        try {
          // Dispatch once; bubbling carries the event to document and window.
          target.dispatchEvent(new d.defaultView.KeyboardEvent(
            down ? 'keydown' : 'keyup', eventInit
          ));
        } catch (e) {}
      };

      let held = {};

      function set(code, on) {
        if (held[code] !== on) {
          held[code] = on;
          key(code, on);
        }
      }

      // -----------------------------------------
      // 5. DETECT GAMEPLAY, NOT MENUS OR LOGIN
      // -----------------------------------------

      function isElementVisible(d, el) {
        if (!el) return false;
        const style = d.defaultView.getComputedStyle(el);
        const rect = el.getBoundingClientRect();
        return style.display !== 'none' &&
               style.visibility !== 'hidden' &&
               Number(style.opacity || 1) > 0 &&
               rect.width > 0 && rect.height > 0;
      }

      function isGameplayActive() {
        try {
          const d = findGameDoc();
          if (!d || !d.getElementById) return false;

          // If the main menu is visible, touches must pass through normally.
          if (isElementVisible(d, d.getElementById('mainmenuelements'))) {
            return false;
          }

          // At least one in-game HUD element must be visible.
          const signals = [
            'ingamechatbox',
            'ingametextwarnings',
            'ingametutorial',
            'ingamecountdown',
            'ingametextwarning_spectating'
          ];

          return signals.some(id =>
            isElementVisible(d, d.getElementById(id))
          );
        } catch (e) {
          return false;
        }
      }

      let gameplayActive = false;

      // -----------------------------------------
      // 6. FLOATING JOYSTICK — ANYWHERE ON LEFT HALF
      // -----------------------------------------

      const deadZone = 13;
      const maxTravel = 48;
      let stickPointerId = null;
      let stickOriginX = 0;
      let stickOriginY = 0;

      function releaseMovement() {
        [37, 38, 39, 40].forEach(code => set(code, false));
        stickPointerId = null;
        stickVisual.style.display = 'none';
        knob.style.transform = 'translate(0,0)';
      }

      function updateStick(e) {
        let dx = e.clientX - stickOriginX;
        let dy = e.clientY - stickOriginY;
        const length = Math.hypot(dx, dy);

        if (length > maxTravel) {
          dx = dx / length * maxTravel;
          dy = dy / length * maxTravel;
        }

        knob.style.transform =
          'translate(' + dx + 'px,' + dy + 'px)';

        const horizontal = Math.abs(dx) >= deadZone;
        const vertical = Math.abs(dy) >= deadZone;

        // Independent axes allow all eight directions.
        set(37, horizontal && dx < 0);
        set(39, horizontal && dx > 0);
        set(38, vertical && dy < 0);
        set(40, vertical && dy > 0);
      }

      leftZone.addEventListener('pointerdown', e => {
        if (!gameplayActive || stickPointerId !== null) return;

        stickPointerId = e.pointerId;
        stickOriginX = e.clientX;
        stickOriginY = e.clientY;

        stickVisual.style.left = stickOriginX + 'px';
        stickVisual.style.top = stickOriginY + 'px';
        stickVisual.style.display = 'block';

        leftZone.setPointerCapture(e.pointerId);
        updateStick(e);
        e.preventDefault();
      });

      leftZone.addEventListener('pointermove', e => {
        if (gameplayActive && stickPointerId === e.pointerId) {
          updateStick(e);
          e.preventDefault();
        }
      });

      function finishStick(e) {
        if (stickPointerId === e.pointerId) {
          releaseMovement();
        }
      }

      leftZone.addEventListener('pointerup', finishStick);
      leftZone.addEventListener('pointercancel', finishStick);
      leftZone.addEventListener('lostpointercapture', finishStick);

      // -----------------------------------------
      // 7. INVISIBLE X / Z HOLD ZONES ON RIGHT HALF
      // -----------------------------------------

      function bindHoldZone(element, code) {
        let activePointerId = null;

        element.addEventListener('pointerdown', e => {
          if (!gameplayActive || activePointerId !== null) return;

          activePointerId = e.pointerId;
          element.setPointerCapture(e.pointerId);
          set(code, true);
          e.preventDefault();
        });

        function releaseZone(e) {
          if (activePointerId === e.pointerId) {
            activePointerId = null;
            set(code, false);
          }
        }

        element.addEventListener('pointerup', releaseZone);
        element.addEventListener('pointercancel', releaseZone);
        element.addEventListener('lostpointercapture', releaseZone);
      }

      bindHoldZone(xZone, 88);
      bindHoldZone(zZone, 90);

      // Keep controls inactive in menus; release every held key on state change.
      function updateTouchMode() {
        const next = isGameplayActive();
        gameplayActive = next;

        const pointerMode = next ? 'auto' : 'none';
        leftZone.style.pointerEvents = pointerMode;
        xZone.style.pointerEvents = pointerMode;
        zZone.style.pointerEvents = pointerMode;

        if (!next) {
          releaseMovement();
          set(88, false);
          set(90, false);
        }
      }

      setInterval(updateTouchMode, 200);
      updateTouchMode();

      document.addEventListener('visibilitychange', () => {
        if (document.hidden) {
          releaseMovement();
          set(88, false);
          set(90, false);
        }
      });

      window.addEventListener('blur', () => {
        releaseMovement();
        set(88, false);
        set(90, false);
      });

    })();
    """.trimIndent()
}
