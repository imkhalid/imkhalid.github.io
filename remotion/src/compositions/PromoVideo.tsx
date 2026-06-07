import {
  AbsoluteFill,
  Audio,
  Easing,
  Sequence,
  Video,
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
const BG2    = "#14112A";
const TEXT   = "#F0EEF8";
const TEXT_MUTED = "rgba(240,238,248,0.6)";

// ── Helpers ───────────────────────────────────────────────────────────────────

function useFadeIn(startFrame: number, dur = 18) {
  const f = useCurrentFrame();
  return interpolate(f, [startFrame, startFrame + dur], [0, 1], {
    extrapolateLeft: "clamp",
    extrapolateRight: "clamp",
    easing: Easing.out(Easing.cubic),
  });
}

function useSpring(startFrame: number) {
  const f = useCurrentFrame();
  const { fps } = useVideoConfig();
  return spring({ frame: f - startFrame, fps, config: { damping: 14, stiffness: 100 } });
}

// ── Scene 1: Intro (3 s) ──────────────────────────────────────────────────────

function IntroScene() {
  const frame = useCurrentFrame();
  const { fps } = useVideoConfig();

  const logoP  = spring({ frame, fps, config: { damping: 11, stiffness: 90, mass: 0.8 } });
  const nameOp = useFadeIn(16, 18);
  const tagOp  = useFadeIn(30, 20);

  const nameY = (() => {
    const p = spring({ frame: frame - 16, fps, config: { damping: 14, stiffness: 90 } });
    return interpolate(p, [0, 1], [30, 0]);
  })();
  const tagY = (() => {
    const p = spring({ frame: frame - 30, fps, config: { damping: 14, stiffness: 90 } });
    return interpolate(p, [0, 1], [30, 0]);
  })();

  return (
    <AbsoluteFill
      style={{
        background: BG,
        alignItems: "center",
        justifyContent: "center",
        flexDirection: "column",
        gap: 28,
      }}
    >
      {/* Glow */}
      <div style={{
        position: "absolute",
        width: 700, height: 700, borderRadius: "50%",
        background: `radial-gradient(circle, ${PURPLE}22 0%, transparent 70%)`,
        transform: `scale(${0.8 + 0.2 * logoP})`,
      }} />

      {/* Logo mark */}
      <div style={{
        transform: `scale(${logoP})`,
        width: 130, height: 130,
        background: `linear-gradient(135deg, ${TEAL}, ${PURPLE})`,
        borderRadius: 36,
        display: "flex", alignItems: "center", justifyContent: "center",
        boxShadow: `0 0 80px ${TEAL}44, 0 0 160px ${PURPLE}22`,
      }}>
        <span style={{ fontFamily: "system-ui,sans-serif", fontWeight: 900, fontSize: 68, color: BG, letterSpacing: -3 }}>C</span>
      </div>

      {/* Name */}
      <div style={{ opacity: nameOp, transform: `translateY(${nameY}px)`, textAlign: "center" }}>
        <div style={{ fontFamily: "system-ui,sans-serif", fontWeight: 800, fontSize: 78, color: TEXT, letterSpacing: -3 }}>
          Cash<span style={{ color: TEAL }}>lytics</span>
        </div>
        <div style={{ fontFamily: "system-ui,sans-serif", fontWeight: 500, fontSize: 22, color: TEXT_MUTED, letterSpacing: 3, textTransform: "uppercase", marginTop: 6 }}>
          Finance & Notes
        </div>
      </div>

      {/* Tagline */}
      <div style={{ opacity: tagOp, transform: `translateY(${tagY}px)`, fontFamily: "system-ui,sans-serif", fontSize: 28, color: TEXT_MUTED, textAlign: "center", lineHeight: 1.5 }}>
        Auto-track bank transactions from{" "}
        <span style={{ color: TEAL }}>SMS alerts</span>
      </div>
    </AbsoluteFill>
  );
}

// ── Callout badge (used in showcase) ─────────────────────────────────────────

function Callout({
  icon, text, startFrame, side = "right",
}: {
  icon: string; text: string; startFrame: number; side?: "left" | "right";
}) {
  const frame  = useCurrentFrame();
  const { fps } = useVideoConfig();
  const p = spring({ frame: frame - startFrame, fps, config: { damping: 14, stiffness: 110 } });
  const op = interpolate(p, [0, 1], [0, 1]);
  const x  = interpolate(p, [0, 1], [side === "right" ? 80 : -80, 0]);

  // fade out 1.5 s before next callout (60 frames per badge cycle, fade in 30 frames)
  const outStart = startFrame + 130;
  const outOp = interpolate(frame, [outStart, outStart + 20], [1, 0], {
    extrapolateLeft: "clamp", extrapolateRight: "clamp",
  });

  return (
    <div style={{
      display: "flex", alignItems: "center", gap: 14,
      background: `${BG2}f2`,
      border: `1.5px solid ${TEAL}55`,
      borderRadius: 60,
      paddingTop: 16, paddingBottom: 16, paddingLeft: 22, paddingRight: 28,
      opacity: op * outOp,
      transform: `translateX(${x}px)`,
      boxShadow: `0 8px 32px ${BG}cc`,
    }}>
      <span style={{ fontSize: 32 }}>{icon}</span>
      <span style={{ fontFamily: "system-ui,sans-serif", fontWeight: 600, fontSize: 24, color: TEXT }}>
        {text}
      </span>
    </div>
  );
}

// ── Scene 2: Showcase — phone frame + live recording (45 s) ──────────────────

const CALLOUTS = [
  { icon: "📱", text: "Auto SMS scan",    startFrame: 15,  side: "right" as const },
  { icon: "📊", text: "Finance ledger",   startFrame: 165, side: "left"  as const },
  { icon: "🔒", text: "100% Offline",     startFrame: 315, side: "right" as const },
  { icon: "📝", text: "Smart notes",      startFrame: 465, side: "left"  as const },
  { icon: "☁️", text: "Google Drive",    startFrame: 615, side: "right" as const },
  { icon: "🛡️", text: "Privacy-first",   startFrame: 765, side: "left"  as const },
  { icon: "💳", text: "Multi-bank ready", startFrame: 915, side: "right" as const },
  { icon: "⚡", text: "Zero manual entry",startFrame: 1065,side: "left"  as const },
];

function ShowcaseScene() {
  const frame  = useCurrentFrame();
  const { fps } = useVideoConfig();

  const PHONE_W = 540;
  const PHONE_H = 1080;

  const phoneP = spring({ frame, fps, config: { damping: 14, stiffness: 70 } });
  const phoneScale = interpolate(phoneP, [0, 1], [0.85, 1]);
  const phoneOp    = interpolate(phoneP, [0, 1], [0, 1]);

  return (
    <AbsoluteFill style={{ background: BG, alignItems: "center", justifyContent: "center" }}>
      {/* Ambient glow behind phone */}
      <div style={{
        position: "absolute",
        width: 600, height: 1200, borderRadius: 300,
        background: `radial-gradient(ellipse, ${TEAL}12 0%, transparent 60%)`,
      }} />

      {/* Callout badges */}
      {CALLOUTS.map((c) => (
        <div
          key={c.startFrame}
          style={{
            position: "absolute",
            [c.side]: 20,
            top: "50%",
            transform: "translateY(-50%)",
          }}
        >
          <Callout {...c} />
        </div>
      ))}

      {/* Phone frame */}
      <div style={{
        width: PHONE_W, height: PHONE_H,
        borderRadius: 56,
        background: "#0a0a12",
        border: "2.5px solid rgba(255,255,255,0.15)",
        overflow: "hidden",
        position: "relative",
        transform: `scale(${phoneScale})`,
        opacity: phoneOp,
        boxShadow: `
          0 0 0 1px rgba(255,255,255,0.05),
          0 40px 120px rgba(0,0,0,0.85),
          0 0 80px ${TEAL}1a
        `,
        flexShrink: 0,
      }}>
        {/* Dynamic island */}
        <div style={{
          position: "absolute", top: 14, left: "50%",
          transform: "translateX(-50%)",
          width: 120, height: 34, background: "#000",
          borderRadius: 17, zIndex: 10,
        }} />

        {/* Screen recording */}
        <Video
          src={staticFile("cashlytics-recording.mp4")}
          style={{ width: "100%", height: "100%", objectFit: "cover" }}
          startFrom={0}
          volume={0}
        />

        {/* Screen glass sheen */}
        <div style={{
          position: "absolute", inset: 0,
          background: "linear-gradient(135deg, rgba(255,255,255,0.04) 0%, transparent 50%)",
          pointerEvents: "none",
        }} />
      </div>
    </AbsoluteFill>
  );
}

// ── Scene 3: Outro (7 s) ──────────────────────────────────────────────────────

function OutroScene() {
  const frame = useCurrentFrame();
  const { fps } = useVideoConfig();

  const logoP  = spring({ frame, fps, config: { damping: 11, stiffness: 90, mass: 0.8 } });
  const nameOp = useFadeIn(14, 18);
  const subOp  = useFadeIn(24, 18);
  const ctaOp  = useFadeIn(36, 20);
  const pillOp = useFadeIn(50, 18);

  const nameY = (() => {
    const p = spring({ frame: frame - 14, fps, config: { damping: 14, stiffness: 90 } });
    return interpolate(p, [0, 1], [30, 0]);
  })();
  const ctaY = (() => {
    const p = spring({ frame: frame - 36, fps, config: { damping: 14, stiffness: 90 } });
    return interpolate(p, [0, 1], [30, 0]);
  })();

  const glow = interpolate(frame, [0, 90], [0.8, 1.2], { extrapolateRight: "clamp" });

  return (
    <AbsoluteFill style={{ background: BG, alignItems: "center", justifyContent: "center", flexDirection: "column", gap: 34 }}>
      <div style={{
        position: "absolute", width: 700, height: 700, borderRadius: "50%",
        background: `radial-gradient(circle, ${TEAL}18 0%, transparent 65%)`,
        transform: `scale(${glow})`,
      }} />

      {/* Logo */}
      <div style={{
        transform: `scale(${logoP})`,
        width: 120, height: 120,
        background: `linear-gradient(135deg, ${TEAL}, ${PURPLE})`,
        borderRadius: 34,
        display: "flex", alignItems: "center", justifyContent: "center",
        boxShadow: `0 0 80px ${TEAL}55`,
      }}>
        <span style={{ fontFamily: "system-ui,sans-serif", fontWeight: 900, fontSize: 62, color: BG }}>C</span>
      </div>

      {/* Name + sub */}
      <div style={{ opacity: nameOp, transform: `translateY(${nameY}px)`, textAlign: "center" }}>
        <div style={{ fontFamily: "system-ui,sans-serif", fontWeight: 800, fontSize: 82, color: TEXT, letterSpacing: -3 }}>
          Cash<span style={{ color: TEAL }}>lytics</span>
        </div>
        <div style={{ opacity: subOp, fontFamily: "system-ui,sans-serif", fontSize: 28, color: TEXT_MUTED, marginTop: 8 }}>
          Your personal finance & notes app
        </div>
      </div>

      {/* CTA */}
      <div style={{
        opacity: ctaOp, transform: `translateY(${ctaY}px)`,
        background: TEAL, color: BG,
        fontFamily: "system-ui,sans-serif", fontWeight: 800, fontSize: 30,
        paddingTop: 24, paddingBottom: 24, paddingLeft: 56, paddingRight: 56,
        borderRadius: 60, display: "flex", alignItems: "center", gap: 14,
        boxShadow: `0 8px 40px ${TEAL}55`,
      }}>
        <span style={{ fontSize: 34 }}>▶</span> Get it on Google Play
      </div>

      {/* Privacy pills */}
      <div style={{ opacity: pillOp, display: "flex", gap: 16, flexWrap: "wrap", justifyContent: "center", maxWidth: 900 }}>
        {["🔒 Offline-first", "📵 No data selling", "SMS stays on-device", "⭐ Premium available"].map((p) => (
          <div key={p} style={{
            fontFamily: "system-ui,sans-serif", fontSize: 20, color: TEAL,
            background: `${TEAL}14`, border: `1px solid ${TEAL}33`,
            borderRadius: 30, paddingTop: 12, paddingBottom: 12, paddingLeft: 22, paddingRight: 22,
          }}>{p}</div>
        ))}
      </div>
    </AbsoluteFill>
  );
}

// ── Root ──────────────────────────────────────────────────────────────────────

export function PromoVideo() {
  const { fps } = useVideoConfig();
  const INTRO    = Math.round(3  * fps);
  const SHOWCASE = Math.round(45 * fps);
  const OUTRO    = Math.round(7  * fps);

  return (
    <AbsoluteFill style={{ background: BG }}>
      {/* Voiceover — plays from the very start across all scenes */}
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
