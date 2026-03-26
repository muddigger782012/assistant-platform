# J.A.R.V.I.S. Main Screen Layout Map

This document is a structural reference for `app/src/main/res/layout/activity_main.xml`.
It is intended for future UI edits without changing behavior accidentally.

## Top-level hierarchy

```text
activity_main.xml
└─ Root LinearLayout (vertical, full screen, bg_jarvis_space)
   ├─ Header panel (brand/icon/title)
   ├─ HorizontalScrollView [headerTabScroller]
   │  └─ LinearLayout [headerTabContainer]
   │     ├─ btnHeaderAssistant
   │     ├─ btnHeaderProject
   │     ├─ btnHeaderShizuku
   │     ├─ btnHeaderStatus
   │     ├─ btnHeaderTerminal
   │     ├─ btnHeaderAudit
   │     ├─ btnHeaderVoice
   │     ├─ btnHeaderPermissions
   │     └─ btnHeaderDhizuku
   └─ FrameLayout [tabContainer]
      ├─ tabAssistantSection (ScrollView)
      ├─ tabProjectSection (ScrollView)
      ├─ tabShizukuSection (ScrollView)
      ├─ tabStatusSection (ScrollView)
      ├─ tabTerminalSection (ScrollView)
      ├─ tabAuditSection (ScrollView)
      ├─ tabVoiceSection (ScrollView)
      ├─ tabPermissionsSection (ScrollView)
      ├─ tabDhizukuSection (ScrollView)
      ├─ vHudPulseOverlay (visual overlay)
      └─ vScanline (visual overlay)
```

## Header tab to section mapping

```text
btnHeaderAssistant   -> tabAssistantSection
btnHeaderProject     -> tabProjectSection
btnHeaderShizuku     -> tabShizukuSection
btnHeaderStatus      -> tabStatusSection
btnHeaderTerminal    -> tabTerminalSection
btnHeaderAudit       -> tabAuditSection
btnHeaderVoice       -> tabVoiceSection
btnHeaderPermissions -> tabPermissionsSection
btnHeaderDhizuku     -> tabDhizukuSection
```

## Section content quick reference

- `tabAssistantSection`
  - `etCommandInput`, `btnRunAssistantCommand`, `btnAssistantQuickStatus`, `tvOutputLog`
- `tabProjectSection`
  - `btnCreateProject`
- `tabShizukuSection`
  - `tvShizukuRuntimeStatus`, `btnRefreshShizukuStatus`, `btnOpenShizukuApp`,
    `btnRequestShizukuPermission`, `etShizukuCommand`, `btnRunShizuku`
- `tabStatusSection`
  - `btnShowStatus`, `tvStatusOutput`, `btnAutoUpgradeApp`, `tvUpdateStatus`
- `tabTerminalSection`
  - `etTerminalCommand`, `btnTerminalRunCommand`, `btnTerminalStopCommand`,
    `btnTerminalClearOutput`, `tvTerminalOutput`, `tvTerminalHistory`
- `tabAuditSection`
  - `etAuditLimit`, `etAuditStatusFilter`, `etAuditAdapterFilter`,
    `btnAuditRefresh`, `btnAuditCopy`, `btnAuditClearView`, `tvAuditDebugOutput`
- `tabVoiceSection`
  - `tvVoiceSectionTitle`, `tvMicPermissionStatus`, `btnGrantMicPermission`,
    `btnToggleVoice`, `btnVoiceSettings`
- `tabPermissionsSection`
  - `btnRequestAllRuntimePermissions`, `btnRequestAllSpecialPermissions`,
    `btnOpenOverlayPermission`, `btnOpenWriteSettingsPermission`,
    `btnOpenAllFilesPermission`, `btnOpenUsageAccessPermission`,
    `btnOpenBatteryOptimizationPermission`, `btnOpenNotificationPolicyPermission`,
    `btnOpenAccessibilitySettings`, `tvSpecialPermissionsStatus`
- `tabDhizukuSection`
  - privilege controls + device owner controls
  - key IDs: `tvPrivilegeCenter`, `switchCameraDisabled`, `switchScreenCaptureDisabled`,
    `switchInstallAppsRestricted`, `switchUninstallAppsRestricted`,
    `switchStatusBarDisabled`, `switchPackageUninstallBlocked`,
    `etLockScreenMessage`, `etPolicyPackageName`,
    `btnApplyCameraDisabled`, `btnApplyScreenCaptureDisabled`,
    `btnApplyInstallAppsRestriction`, `btnApplyUninstallAppsRestriction`,
    `btnApplyStatusBarDisabled`, `btnApplyLockScreenMessage`,
    `btnApplyPackagePolicy`, `btnLockNow`, `btnRebootFromDhizuku`

