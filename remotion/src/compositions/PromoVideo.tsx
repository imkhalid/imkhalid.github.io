import {
  AbsoluteFill,
  Audio,
  Easing,
  OffthreadVideo,
  Sequence,
  interpolate,
  spring,
  staticFile,
  useCurrentFrame,
  useVideoConfig,
} from "remotion";

// ── Design tokens ─────────────────────────────────────────────────────────────
const TEAL   = "#4DD9AC";
const PURPLE = "#7F77DD";
const BG     = "#0C0A1A";
const TEXT   = "#F0EEF8";
const MUTED  = "rgba(240,238,248,0.65)";

// ── Helpers ───────────────────────────────────────────────────────────────────

function fadeIn(start: number, dur = 18) {
  const f = useCurrentFrame();
  return interpolate(f, [start, start + dur], [0, 1], {
    extrapolateLeft: "clamp", extrapolateRight: "clamp",
    easing: Easing.out(Easing.cubic),
  });
}

function spr(startFrame: number, cfg = { damping: 14, stiffness: 100 }) {
  const f = useCurrentFrame();
  const { fps } = useVideoConfig();
  return spring({ frame: f - startFrame, fps, config: cfg });
}

function slideUp(startFrame: number, amount = 30) {
  const p = spr(startFrame, { damping: 14, stiffness: 90 });
  return interpolate(p, [0, 1], [amount, 0]);
}

// ── INTRO (3 s) ───────────────────────────────────────────────────────────────

function IntroScene() {
  const frame = useCurrentFrame();
  const { fps } = useVideoConfig();

  const logoP  = spring({ frame, fps, config: { damping: 11, stiffness: 90, mass: 0.8 } });
  const nameOp = fadeIn(16, 18);
  const tagOp  = fadeIn(30, 20);
  const nameY  = slideUp(16);
  const tagY   = slideUp(30);
  const glowP  = interpolate(frame, [0, 90], [0.7, 1.2], { extrapolateRight: "clamp" });

  return (
    <AbsoluteFill style={{ background: BG, alignItems: "center", justifyContent: "center", flexDirection: "column", gap: 32 }}>
      <div style={{ position: "absolute", width: 800, height: 800, borderRadius: "50%", background: `radial-gradient(circle, ${PURPLE}22 0%, transparent 70%)`, transform: `scale(${glowP})` }} />

      <div style={{ transform: `scale(${logoP})`, width: 140, height: 140, background: `linear-gradient(135deg, ${TEAL}, ${PURPLE})`, borderRadius: 38, display: "flex", alignItems: "center", justifyContent: "center", boxShadow: `0 0 80px ${TEAL}55, 0 0 160px ${PURPLE}22` }}>
        <span style={{ fontFamily: "system-ui,sans-serif", fontWeight: 900, fontSize: 72, color: BG }}>C</span>
      </div>

      <div style={{ opacity: nameOp, transform: `translateY(${nameY}px)`, textAlign: "center" }}>
        <div style={{ fontFamily: "system-ui,sans-serif", fontWeight: 800, fontSize: 84, color: TEXT, letterSpacing: -3 }}>
          Cash<span style={{ color: TEAL }}>lytics</span>
        </div>
        <div style={{ fontFamily: "system-ui,sans-serif", fontWeight: 500, fontSize: 24, color: MUTED, letterSpacing: 3, textTransform: "uppercase", marginTop: 8 }}>
          Finance & Notes
        </div>
      </div>

      <div style={{ opacity: tagOp, transform: `translateY(${tagY}px)`, fontFamily: "system-ui,sans-serif", fontSize: 30, color: MUTED, textAlign: "center", lineHeight: 1.5, maxWidth: 800 }}>
        Auto-track bank transactions from <span style={{ color: TEAL }}>SMS alerts</span>
      </div>
    </AbsoluteFill>
  );
}

// ── SHOWCASE (45 s) — recording fills canvas ──────────────────────────────────

const BADGES = [
  { icon: "📱", text: "Auto SMS Detection",   start: 10  },
  { icon: "🏦", text: "All major banks",       start: 160 },
  { icon: "🔒", text: "100% Offline",          start: 310 },
  { icon: "📊", text: "Finance ledger",        start: 460 },
  { icon: "📝", text: "Smart notes",           start: 610 },
  { icon: "☁️", text: "Google Drive backup",  start: 760 },
  { icon: "🛡️", text: "Privacy-first",        start: 910 },
  { icon: "⚡", text: "Zero manual entry",     start: 1060 },
];

function Badge({ icon, text, start }: { icon: string; text: string; start: number }) {
  const frame = useCurrentFrame();
  const { fps } = useVideoConfig();

  const SHOW = 120; // frames visible
  const FADE = 20;

  const inP  = spring({ frame: frame - start, fps, config: { damping: 14, stiffness: 110 } });
  const inOp = interpolate(inP, [0, 1], [0, 1]);
  const inY  = interpolate(inP, [0, 1], [24, 0]);
  const outOp = interpolate(frame, [start + SHOW - FADE, start + SHOW], [1, 0], {
    extrapolateLeft: "clamp", extrapolateRight: "clamp",
    easing: Easing.in(Easing.cubic),
  });
  const opacity = inOp * outOp;

  if (opacity <= 0) return null;

  return (
    <div style={{
      display: "flex", alignItems: "center", gap: 14,
      background: "rgba(12,10,26,0.88)",
      border: `1.5px solid ${TEAL}66`,
      borderRadius: 60,
      paddingTop: 18, paddingBottom: 18, paddingLeft: 24, paddingRight: 32,
      opacity,
      transform: `translateY(${inY}px)`,
      backdropFilter: "blur(12px)",
      boxShadow: `0 8px 40px rgba(0,0,0,0.6), 0 0 0 1px rgba(255,255,255,0.04)`,
    }}>
      <span style={{ fontSize: 34 }}>{icon}</span>
      <span style={{ fontFamily: "system-ui,sans-serif", fontWeight: 700, fontSize: 26, color: TEXT }}>
        {text}
      </span>
    </div>
  );
}

function ShowcaseScene() {
  const frame  = useCurrentFrame();
  const { fps } = useVideoConfig();

  // Phone frame fades in fast
  const phoneP = spring({ frame, fps, config: { damping: 14, stiffness: 80 } });
  const frameOp = interpolate(phoneP, [0, 1], [0, 1]);

  // Output is 1080×1920. Recording is same resolution.
  // Show recording at full width with thin phone-border overlay.
  // Leave 40px padding on sides, 60px top/bottom for the border feel.
  const PAD_X = 0;
  const PAD_TOP = 0;
  const PAD_BOT = 0;
  const RADIUS = 0;

  return (
    <AbsoluteFill style={{ background: "#000" }}>
      {/* ── Recording fills entire canvas ── */}
      {/* OffthreadVideo handles non-seekable & high-fps recordings correctly */}
      <OffthreadVideo
        src={staticFile("cashlytics-recording.mp4")}
        style={{
          position: "absolute",
          top: 0, left: 0,
          width: "100%",
          height: "100%",
          objectFit: "cover",
        }}
        startFrom={0}
        volume={0}
      />

      {/* ── Gradient top bar ── */}
      <div style={{
        position: "absolute", top: 0, left: 0, right: 0, height: 220,
        background: "linear-gradient(to bottom, rgba(12,10,26,0.95) 0%, rgba(12,10,26,0) 100%)",
        display: "flex", alignItems: "flex-start", padding: "40px 44px 0",
        gap: 18, opacity: frameOp,
      }}>
        <div style={{ width: 56, height: 56, background: `linear-gradient(135deg, ${TEAL}, ${PURPLE})`, borderRadius: 16, display: "flex", alignItems: "center", justifyContent: "center", flexShrink: 0 }}>
          <span style={{ fontFamily: "system-ui,sans-serif", fontWeight: 900, fontSize: 30, color: BG }}>C</span>
        </div>
        <div>
          <div style={{ fontFamily: "system-ui,sans-serif", fontWeight: 800, fontSize: 36, color: TEXT, letterSpacing: -1 }}>
            Cash<span style={{ color: TEAL }}>lytics</span>
          </div>
          <div style={{ fontFamily: "system-ui,sans-serif", fontSize: 18, color: MUTED }}>Finance & Notes</div>
        </div>
      </div>

      {/* ── Gradient bottom bar with animated badges ── */}
      <div style={{
        position: "absolute", bottom: 0, left: 0, right: 0, height: 280,
        background: "linear-gradient(to top, rgba(12,10,26,0.97) 0%, rgba(12,10,26,0) 100%)",
        display: "flex", alignItems: "flex-end", justifyContent: "center",
        paddingBottom: 56, opacity: frameOp,
      }}>
        <div style={{ display: "flex", flexDirection: "column", alignItems: "center", gap: 16 }}>
          {BADGES.map((b) => (
            <Badge key={b.start} {...b} />
          ))}
        </div>
      </div>

      {/* ── Subtle vignette on left+right edges ── */}
      <div style={{ position: "absolute", inset: 0, background: "radial-gradient(ellipse 100% 50% at 50% 50%, transparent 60%, rgba(0,0,0,0.3) 100%)", pointerEvents: "none" }} />
    </AbsoluteFill>
  );
}

// ── OUTRO (7 s) ───────────────────────────────────────────────────────────────

function OutroScene() {
  const frame = useCurrentFrame();
  const { fps } = useVideoConfig();

  const logoP  = spring({ frame, fps, config: { damping: 11, stiffness: 90, mass: 0.8 } });
  const nameOp = fadeIn(14, 18);
  const nameY  = slideUp(14);
  const subOp  = fadeIn(24, 18);
  const ctaOp  = fadeIn(36, 20);
  const ctaY   = slideUp(36);
  const pillOp = fadeIn(50, 18);
  const glow   = interpolate(frame, [0, 90], [0.8, 1.25], { extrapolateRight: "clamp" });

  return (
    <AbsoluteFill style={{ background: BG, alignItems: "center", justifyContent: "center", flexDirection: "column", gap: 36 }}>
      <div style={{ position: "absolute", width: 800, height: 800, borderRadius: "50%", background: `radial-gradient(circle, ${TEAL}18 0%, transparent 65%)`, transform: `scale(${glow})` }} />

      <div style={{ transform: `scale(${logoP})`, width: 130, height: 130, background: `linear-gradient(135deg, ${TEAL}, ${PURPLE})`, borderRadius: 36, display: "flex", alignItems: "center", justifyContent: "center", boxShadow: `0 0 80px ${TEAL}55` }}>
        <span style={{ fontFamily: "system-ui,sans-serif", fontWeight: 900, fontSize: 66, color: BG }}>C</span>
      </div>

      <div style={{ opacity: nameOp, transform: `translateY(${nameY}px)`, textAlign: "center" }}>
        <div style={{ fontFamily: "system-ui,sans-serif", fontWeight: 800, fontSize: 88, color: TEXT, letterSpacing: -4 }}>
          Cash<span style={{ color: TEAL }}>lytics</span>
        </div>
        <div style={{ opacity: subOp, fontFamily: "system-ui,sans-serif", fontSize: 30, color: MUTED, marginTop: 10 }}>
          Your personal finance & notes app
        </div>
      </div>

      <div style={{ opacity: ctaOp, transform: `translateY(${ctaY}px)`, background: TEAL, color: BG, fontFamily: "system-ui,sans-serif", fontWeight: 800, fontSize: 32, paddingTop: 26, paddingBottom: 26, paddingLeft: 60, paddingRight: 60, borderRadius: 70, display: "flex", alignItems: "center", gap: 16, boxShadow: `0 8px 48px ${TEAL}55` }}>
        <span style={{ fontSize: 36 }}>▶</span> Get it on Google Play
      </div>

      <div style={{ opacity: pillOp, display: "flex", gap: 16, flexWrap: "wrap", justifyContent: "center", maxWidth: 960 }}>
        {["🔒 Offline-first", "📵 No data selling", "SMS stays on-device", "⭐ Premium available"].map((p) => (
          <div key={p} style={{ fontFamily: "system-ui,sans-serif", fontSize: 22, color: TEAL, background: `${TEAL}14`, border: `1px solid ${TEAL}33`, borderRadius: 34, paddingTop: 13, paddingBottom: 13, paddingLeft: 24, paddingRight: 24 }}>{p}</div>
        ))}
      </div>
    </AbsoluteFill>
  );
}

// ── Root ──────────────────────────────────────────────────────────────────────

export function PromoVideo() {
  const { fps } = useVideoConfig();
  const INTRO    = Math.round(3  * fps);
  const SHOWCASE = Math.round(60 * fps);  // show first 60 s of the 105 s recording
  const OUTRO    = Math.round(7  * fps);

  return (
    <AbsoluteFill style={{ background: BG }}>
      <Audio src={staticFile("voiceover.wav")} volume={1} />

      <Sequence durationInFrames={INTRO}>
        <IntroScene />
      </Sequence>

      <Sequence from={INTRO} durationInFrames={SHOWCASE}>
        <ShowcaseScene />
      </Sequence>

      <Sequence from={INTRO + SHOWCASE} durationInFrames={OUTRO}>
        <OutroScene />
      </Sequence>
    </AbsoluteFill>
  );
}
