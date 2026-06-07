import {
  AbsoluteFill,
  Easing,
  Sequence,
  interpolate,
  spring,
  useCurrentFrame,
  useVideoConfig,
} from "remotion";

const TEAL = "#4DD9AC";
const PURPLE = "#7F77DD";
const BG = "#0C0A1A";
const BG2 = "#14112A";
const BG3 = "#1C1830";
const TEXT = "#F0EEF8";
const TEXT_MUTED = "rgba(240,238,248,0.55)";
const TEXT_DIM = "rgba(240,238,248,0.35)";
const BORDER = "rgba(255,255,255,0.08)";
const RED = "#F87171";
const GREEN = "#4DD9AC";

// ── helpers ──────────────────────────────────────────────────────────────────

function useFadeIn(startFrame: number, durationFrames = 20) {
  const frame = useCurrentFrame();
  return interpolate(frame, [startFrame, startFrame + durationFrames], [0, 1], {
    extrapolateLeft: "clamp",
    extrapolateRight: "clamp",
    easing: Easing.out(Easing.cubic),
  });
}

function useSlideUp(startFrame: number, durationFrames = 25, amount = 30) {
  const frame = useCurrentFrame();
  const { fps } = useVideoConfig();
  const p = spring({ frame: frame - startFrame, fps, config: { damping: 14, stiffness: 90 } });
  return interpolate(p, [0, 1], [amount, 0]);
}

// ── Intro (0 – 3 s) ──────────────────────────────────────────────────────────

function IntroScene() {
  const frame = useCurrentFrame();
  const { fps } = useVideoConfig();

  const logoScale = spring({ frame, fps, config: { damping: 11, stiffness: 90, mass: 0.8 } });
  const nameOpacity = useFadeIn(16, 18);
  const nameY = useSlideUp(16, 22);
  const tagOpacity = useFadeIn(28, 20);
  const tagY = useSlideUp(28, 25);

  const glowScale = interpolate(frame, [0, 90], [0.8, 1.15], { extrapolateRight: "clamp" });

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
      {/* Background glow */}
      <div style={{
        position: "absolute",
        width: 600,
        height: 600,
        borderRadius: "50%",
        background: `radial-gradient(circle, ${PURPLE}22 0%, transparent 70%)`,
        transform: `scale(${glowScale})`,
      }} />

      {/* Logo */}
      <div style={{
        transform: `scale(${logoScale})`,
        width: 120,
        height: 120,
        background: `linear-gradient(135deg, ${TEAL}, ${PURPLE})`,
        borderRadius: 34,
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        boxShadow: `0 0 80px ${TEAL}44, 0 0 160px ${PURPLE}22`,
      }}>
        <span style={{ fontFamily: "system-ui,sans-serif", fontWeight: 900, fontSize: 62, color: BG, letterSpacing: -3 }}>C</span>
      </div>

      {/* Name */}
      <div style={{ opacity: nameOpacity, transform: `translateY(${nameY}px)`, display: "flex", flexDirection: "column", alignItems: "center", gap: 8 }}>
        <div style={{ fontFamily: "system-ui,sans-serif", fontWeight: 800, fontSize: 72, color: TEXT, letterSpacing: -3 }}>
          Cash<span style={{ color: TEAL }}>lytics</span>
        </div>
        <div style={{ fontFamily: "system-ui,sans-serif", fontWeight: 500, fontSize: 22, color: TEXT_MUTED, letterSpacing: 2, textTransform: "uppercase" }}>
          Finance & Notes
        </div>
      </div>

      {/* Tagline */}
      <div style={{ opacity: tagOpacity, transform: `translateY(${tagY}px)`, fontFamily: "system-ui,sans-serif", fontSize: 28, color: TEXT_MUTED, textAlign: "center", maxWidth: 700, lineHeight: 1.5 }}>
        Your finances — <span style={{ color: TEAL }}>auto-tracked</span> from bank SMS alerts
      </div>
    </AbsoluteFill>
  );
}

// ── Mock App UI ───────────────────────────────────────────────────────────────

const TRANSACTIONS = [
  { label: "HBL Bank Alert", desc: "PKR 15,000 credited", amount: "+15,000", type: "credit", icon: "🏦" },
  { label: "Meezan Bank", desc: "PKR 3,200 debited", amount: "-3,200", type: "debit", icon: "💳" },
  { label: "EasyPaisa", desc: "PKR 500 debited", amount: "-500", type: "debit", icon: "📱" },
  { label: "UBL Salary", desc: "PKR 85,000 credited", amount: "+85,000", type: "credit", icon: "💼" },
  { label: "Alfalah Bank", desc: "PKR 7,800 debited", amount: "-7,800", type: "debit", icon: "🏧" },
];

function TransactionRow({ tx, delay }: { tx: typeof TRANSACTIONS[0]; delay: number }) {
  const frame = useCurrentFrame();
  const { fps } = useVideoConfig();
  const p = spring({ frame: frame - delay, fps, config: { damping: 14, stiffness: 100 } });
  const opacity = interpolate(p, [0, 1], [0, 1]);
  const x = interpolate(p, [0, 1], [-30, 0]);

  return (
    <div style={{
      display: "flex", alignItems: "center", gap: 18,
      padding: "18px 24px",
      background: BG3,
      borderRadius: 16,
      border: `1px solid ${BORDER}`,
      opacity,
      transform: `translateX(${x}px)`,
    }}>
      <div style={{ width: 52, height: 52, borderRadius: 14, background: BG2, display: "flex", alignItems: "center", justifyContent: "center", fontSize: 26, flexShrink: 0 }}>
        {tx.icon}
      </div>
      <div style={{ flex: 1, minWidth: 0 }}>
        <div style={{ fontFamily: "system-ui,sans-serif", fontWeight: 600, fontSize: 20, color: TEXT, whiteSpace: "nowrap", overflow: "hidden", textOverflow: "ellipsis" }}>{tx.label}</div>
        <div style={{ fontFamily: "system-ui,sans-serif", fontSize: 16, color: TEXT_MUTED, marginTop: 2 }}>{tx.desc}</div>
      </div>
      <div style={{ fontFamily: "system-ui,sans-serif", fontWeight: 700, fontSize: 22, color: tx.type === "credit" ? GREEN : RED, flexShrink: 0 }}>
        {tx.amount}
      </div>
    </div>
  );
}

function BarChart({ heights, delayStart }: { heights: number[]; delayStart: number }) {
  const frame = useCurrentFrame();
  const { fps } = useVideoConfig();
  const barColors = [TEXT_DIM, TEXT_DIM, TEAL, TEXT_DIM, TEAL, TEXT_DIM, PURPLE];
  return (
    <div style={{ display: "flex", alignItems: "flex-end", gap: 10, height: 80, paddingTop: 8 }}>
      {heights.map((h, i) => {
        const p = spring({ frame: frame - delayStart - i * 3, fps, config: { damping: 14, stiffness: 80 } });
        return (
          <div key={i} style={{ flex: 1, height: `${h * p}%`, background: barColors[i % barColors.length], borderRadius: "4px 4px 0 0", minHeight: 0 }} />
        );
      })}
    </div>
  );
}

function MockAppScreen() {
  const frame = useCurrentFrame();
  const headerOpacity = useFadeIn(0, 15);
  const balanceScale = spring({ frame, fps: 30, config: { damping: 13, stiffness: 80 } });

  return (
    <div style={{
      width: "100%", height: "100%",
      background: BG,
      display: "flex", flexDirection: "column",
      fontFamily: "system-ui,sans-serif",
      overflow: "hidden",
    }}>
      {/* Status bar */}
      <div style={{ height: 44, background: BG2, display: "flex", alignItems: "center", justifyContent: "space-between", padding: "0 28px", flexShrink: 0 }}>
        <span style={{ fontSize: 16, color: TEXT_MUTED, fontWeight: 600 }}>9:41</span>
        <div style={{ display: "flex", gap: 8 }}>
          {["▋▋▋▋", "●●●", "⚡"].map((s, i) => (
            <span key={i} style={{ fontSize: 14, color: TEXT_MUTED }}>{s}</span>
          ))}
        </div>
      </div>

      {/* App header */}
      <div style={{ padding: "20px 28px 0", opacity: headerOpacity, flexShrink: 0 }}>
        <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
          <div>
            <div style={{ fontSize: 14, color: TEXT_MUTED, marginBottom: 2 }}>Good morning,</div>
            <div style={{ fontSize: 26, fontWeight: 700, color: TEXT }}>Khalid 👋</div>
          </div>
          <div style={{ width: 48, height: 48, borderRadius: 14, background: `linear-gradient(135deg, ${TEAL}, ${PURPLE})`, display: "flex", alignItems: "center", justifyContent: "center", fontWeight: 800, fontSize: 20, color: BG }}>K</div>
        </div>
      </div>

      {/* Balance card */}
      <div style={{
        margin: "20px 20px 0",
        padding: "24px 28px",
        background: `linear-gradient(135deg, ${PURPLE}88, ${TEAL}44)`,
        borderRadius: 24,
        border: `1px solid ${TEAL}33`,
        opacity: headerOpacity,
        transform: `scale(${0.85 + 0.15 * balanceScale})`,
        flexShrink: 0,
      }}>
        <div style={{ fontSize: 16, color: "rgba(240,238,248,0.7)", marginBottom: 6 }}>Total Balance</div>
        <div style={{ fontSize: 46, fontWeight: 800, color: TEXT, letterSpacing: -1 }}>PKR 88,500</div>
        <div style={{ display: "flex", gap: 24, marginTop: 16 }}>
          <div>
            <div style={{ fontSize: 13, color: "rgba(240,238,248,0.6)" }}>Income</div>
            <div style={{ fontSize: 20, fontWeight: 700, color: TEAL }}>+1,00,000</div>
          </div>
          <div style={{ width: 1, background: "rgba(255,255,255,0.15)" }} />
          <div>
            <div style={{ fontSize: 13, color: "rgba(240,238,248,0.6)" }}>Expenses</div>
            <div style={{ fontSize: 20, fontWeight: 700, color: RED }}>-11,500</div>
          </div>
        </div>
      </div>

      {/* Monthly chart */}
      <div style={{ margin: "20px 20px 0", padding: "18px 20px 10px", background: BG2, borderRadius: 20, border: `1px solid ${BORDER}`, flexShrink: 0 }}>
        <div style={{ display: "flex", justifyContent: "space-between", marginBottom: 8 }}>
          <span style={{ fontSize: 16, fontWeight: 600, color: TEXT }}>Monthly Trend</span>
          <span style={{ fontSize: 14, color: TEAL }}>June 2026</span>
        </div>
        <BarChart heights={[55, 70, 45, 85, 60, 95, 50]} delayStart={5} />
        <div style={{ display: "flex", justifyContent: "space-between", marginTop: 6 }}>
          {["M", "T", "W", "T", "F", "S", "S"].map((d, i) => (
            <span key={i} style={{ flex: 1, textAlign: "center", fontSize: 13, color: TEXT_DIM }}>{d}</span>
          ))}
        </div>
      </div>

      {/* Transactions section */}
      <div style={{ padding: "16px 20px 6px", flexShrink: 0 }}>
        <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
          <span style={{ fontSize: 18, fontWeight: 700, color: TEXT }}>Recent Transactions</span>
          <span style={{ fontSize: 14, color: TEAL }}>See all →</span>
        </div>
      </div>

      {/* Transaction list */}
      <div style={{ flex: 1, overflowY: "hidden", padding: "0 20px 20px", display: "flex", flexDirection: "column", gap: 12 }}>
        {TRANSACTIONS.map((tx, i) => (
          <TransactionRow key={i} tx={tx} delay={i * 8 + 15} />
        ))}
      </div>

      {/* Bottom nav */}
      <div style={{
        height: 72, background: BG2,
        borderTop: `1px solid ${BORDER}`,
        display: "flex", alignItems: "center", justifyContent: "space-around",
        flexShrink: 0,
      }}>
        {[{ icon: "🏠", label: "Home", active: true }, { icon: "📊", label: "Stats" }, { icon: "📝", label: "Notes" }, { icon: "⚙️", label: "Settings" }].map((nav, i) => (
          <div key={i} style={{ display: "flex", flexDirection: "column", alignItems: "center", gap: 4 }}>
            <span style={{ fontSize: 24 }}>{nav.icon}</span>
            <span style={{ fontSize: 12, color: nav.active ? TEAL : TEXT_DIM, fontWeight: nav.active ? 700 : 400 }}>{nav.label}</span>
          </div>
        ))}
      </div>
    </div>
  );
}

// ── Showcase (3 s – 28 s) ─────────────────────────────────────────────────────

function FeatureBadge({ text, icon, startFrame, side = "right" }: { text: string; icon: string; startFrame: number; side?: "left" | "right" }) {
  const frame = useCurrentFrame();
  const { fps } = useVideoConfig();
  const p = spring({ frame: frame - startFrame, fps, config: { damping: 14, stiffness: 120 } });
  const opacity = interpolate(p, [0, 1], [0, 1]);
  const x = interpolate(p, [0, 1], [side === "right" ? 60 : -60, 0]);
  return (
    <div style={{ display: "flex", alignItems: "center", gap: 14, background: `${BG2}f0`, border: `1px solid ${TEAL}44`, borderRadius: 50, paddingTop: 14, paddingBottom: 14, paddingLeft: 20, paddingRight: 28, opacity, transform: `translateX(${x}px)`, boxShadow: `0 4px 24px ${BG}aa` }}>
      <span style={{ fontSize: 30 }}>{icon}</span>
      <span style={{ fontFamily: "system-ui,sans-serif", fontWeight: 600, fontSize: 22, color: TEXT }}>{text}</span>
    </div>
  );
}

function ShowcaseScene() {
  const frame = useCurrentFrame();
  const { fps } = useVideoConfig();

  const PHONE_W = 440;
  const PHONE_H = 920;

  const phoneP = spring({ frame, fps, config: { damping: 14, stiffness: 70 } });
  const phoneScale = interpolate(phoneP, [0, 1], [0.85, 1]);
  const phoneOpacity = interpolate(phoneP, [0, 1], [0, 1]);

  return (
    <AbsoluteFill style={{ background: BG, alignItems: "center", justifyContent: "center" }}>
      {/* bg glow */}
      <div style={{ position: "absolute", width: 700, height: 700, borderRadius: "50%", background: `radial-gradient(circle, ${TEAL}0d 0%, transparent 65%)` }} />

      {/* Left badges */}
      <div style={{ position: "absolute", left: 30, top: "50%", transform: "translateY(-50%)", display: "flex", flexDirection: "column", gap: 22 }}>
        <FeatureBadge text="Auto SMS scan" icon="📱" startFrame={8} side="left" />
        <FeatureBadge text="Offline-first" icon="🔒" startFrame={20} side="left" />
        <FeatureBadge text="Finance ledger" icon="📊" startFrame={32} side="left" />
      </div>

      {/* Phone frame */}
      <div style={{
        width: PHONE_W, height: PHONE_H,
        borderRadius: 52,
        background: "#0a0a12",
        border: "2.5px solid rgba(255,255,255,0.14)",
        overflow: "hidden",
        transform: `scale(${phoneScale})`,
        opacity: phoneOpacity,
        boxShadow: `0 40px 120px rgba(0,0,0,0.8), 0 0 0 1px rgba(255,255,255,0.05), 0 0 60px ${TEAL}18`,
      }}>
        <MockAppScreen />
      </div>

      {/* Right badges */}
      <div style={{ position: "absolute", right: 30, top: "50%", transform: "translateY(-50%)", display: "flex", flexDirection: "column", gap: 22 }}>
        <FeatureBadge text="Smart notes" icon="📝" startFrame={14} side="right" />
        <FeatureBadge text="Google Drive" icon="☁️" startFrame={26} side="right" />
        <FeatureBadge text="Privacy-first" icon="🛡️" startFrame={38} side="right" />
      </div>
    </AbsoluteFill>
  );
}

// ── Feature callout (28 s – 38 s) ────────────────────────────────────────────

const FEATURES = [
  { icon: "📱", title: "Auto SMS Detection", desc: "Bank alerts auto-parsed into your ledger — zero manual entry" },
  { icon: "🔒", title: "100% Private", desc: "All data stays on-device. We never see your transactions." },
  { icon: "📊", title: "Smart Analytics", desc: "Spending trends, monthly charts, and category breakdowns" },
  { icon: "📝", title: "Finance + Notes", desc: "Built-in notebook alongside your expense tracker" },
];

function FeatureCalloutScene() {
  const frame = useCurrentFrame();
  const titleOp = useFadeIn(0, 18);
  const titleY = useSlideUp(0, 22);

  return (
    <AbsoluteFill style={{ background: BG, alignItems: "center", justifyContent: "center", flexDirection: "column", gap: 40, padding: "0 60px" }}>
      <div style={{ opacity: titleOp, transform: `translateY(${titleY}px)`, fontFamily: "system-ui,sans-serif", fontWeight: 800, fontSize: 52, color: TEXT, textAlign: "center", letterSpacing: -2 }}>
        Why <span style={{ color: TEAL }}>Cashlytics</span>?
      </div>
      <div style={{ display: "flex", flexDirection: "column", gap: 20, width: "100%", maxWidth: 900 }}>
        {FEATURES.map((f, i) => {
          const { fps } = useVideoConfig();
          const p = spring({ frame: frame - i * 12 - 10, fps, config: { damping: 14, stiffness: 90 } });
          const op = interpolate(p, [0, 1], [0, 1]);
          const x = interpolate(p, [0, 1], [50, 0]);
          return (
            <div key={i} style={{ display: "flex", gap: 24, alignItems: "center", background: BG2, borderRadius: 22, padding: "22px 28px", border: `1px solid ${BORDER}`, opacity: op, transform: `translateX(${x}px)` }}>
              <div style={{ width: 70, height: 70, borderRadius: 18, background: BG3, display: "flex", alignItems: "center", justifyContent: "center", fontSize: 34, flexShrink: 0 }}>{f.icon}</div>
              <div>
                <div style={{ fontFamily: "system-ui,sans-serif", fontWeight: 700, fontSize: 26, color: TEXT, marginBottom: 4 }}>{f.title}</div>
                <div style={{ fontFamily: "system-ui,sans-serif", fontSize: 20, color: TEXT_MUTED, lineHeight: 1.4 }}>{f.desc}</div>
              </div>
            </div>
          );
        })}
      </div>
    </AbsoluteFill>
  );
}

// ── Outro CTA (38 s – 45 s) ──────────────────────────────────────────────────

function OutroScene() {
  const frame = useCurrentFrame();
  const { fps } = useVideoConfig();

  const logoP = spring({ frame, fps, config: { damping: 11, stiffness: 90, mass: 0.8 } });
  const nameOp = useFadeIn(12, 18);
  const nameY = useSlideUp(12, 22);
  const ctaOp = useFadeIn(26, 20);
  const ctaY = useSlideUp(26, 25);
  const pillsOp = useFadeIn(38, 18);

  const glowScale = interpolate(frame, [0, 90], [0.9, 1.2], { extrapolateRight: "clamp" });

  return (
    <AbsoluteFill style={{ background: BG, alignItems: "center", justifyContent: "center", flexDirection: "column", gap: 32 }}>
      <div style={{ position: "absolute", width: 700, height: 700, borderRadius: "50%", background: `radial-gradient(circle, ${TEAL}15 0%, transparent 65%)`, transform: `scale(${glowScale})` }} />

      <div style={{ transform: `scale(${logoP})`, width: 110, height: 110, background: `linear-gradient(135deg, ${TEAL}, ${PURPLE})`, borderRadius: 32, display: "flex", alignItems: "center", justifyContent: "center", boxShadow: `0 0 80px ${TEAL}44` }}>
        <span style={{ fontFamily: "system-ui,sans-serif", fontWeight: 900, fontSize: 58, color: BG, letterSpacing: -2 }}>C</span>
      </div>

      <div style={{ opacity: nameOp, transform: `translateY(${nameY}px)`, textAlign: "center" }}>
        <div style={{ fontFamily: "system-ui,sans-serif", fontWeight: 800, fontSize: 80, color: TEXT, letterSpacing: -4 }}>
          Cash<span style={{ color: TEAL }}>lytics</span>
        </div>
        <div style={{ fontFamily: "system-ui,sans-serif", fontSize: 26, color: TEXT_MUTED, marginTop: 8 }}>
          Your personal finance & notes app
        </div>
      </div>

      {/* CTA button */}
      <div style={{ opacity: ctaOp, transform: `translateY(${ctaY}px)`, background: TEAL, color: BG, fontFamily: "system-ui,sans-serif", fontWeight: 800, fontSize: 28, paddingTop: 22, paddingBottom: 22, paddingLeft: 52, paddingRight: 52, borderRadius: 60, display: "flex", alignItems: "center", gap: 14, boxShadow: `0 8px 40px ${TEAL}55` }}>
        <span style={{ fontSize: 32 }}>▶</span> Get it on Google Play
      </div>

      {/* Pills */}
      <div style={{ opacity: pillsOp, display: "flex", gap: 16, flexWrap: "wrap", justifyContent: "center" }}>
        {["🔒 Offline-first", "📵 No data selling", "SMS stays on-device", "⭐ Premium available"].map((p, i) => (
          <div key={i} style={{ fontFamily: "system-ui,sans-serif", fontSize: 18, color: TEAL, background: `${TEAL}14`, border: `1px solid ${TEAL}33`, borderRadius: 30, paddingTop: 10, paddingBottom: 10, paddingLeft: 20, paddingRight: 20 }}>{p}</div>
        ))}
      </div>
    </AbsoluteFill>
  );
}

// ── Root ──────────────────────────────────────────────────────────────────────

export function PromoVideo() {
  const { fps } = useVideoConfig();
  const INTRO    = Math.round(3  * fps);
  const SHOWCASE = Math.round(25 * fps);
  const CALLOUT  = Math.round(10 * fps);
  const OUTRO    = Math.round(7  * fps);

  return (
    <AbsoluteFill style={{ background: BG }}>
      <Sequence durationInFrames={INTRO}>
        <IntroScene />
      </Sequence>
      <Sequence from={INTRO} durationInFrames={SHOWCASE}>
        <ShowcaseScene />
      </Sequence>
      <Sequence from={INTRO + SHOWCASE} durationInFrames={CALLOUT}>
        <FeatureCalloutScene />
      </Sequence>
      <Sequence from={INTRO + SHOWCASE + CALLOUT} durationInFrames={OUTRO}>
        <OutroScene />
      </Sequence>
    </AbsoluteFill>
  );
}
