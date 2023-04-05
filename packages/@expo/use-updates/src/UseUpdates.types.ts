import Constants from 'expo-constants';
import type { UpdatesLogEntry } from 'expo-updates';

export type ClassicManifest = NonNullable<typeof Constants.manifest>;

export type Manifest = ClassicManifest | NonNullable<typeof Constants.manifest2>;

/**
 * Structure encapsulating information on the currently running app
 * (either the embedded bundle or a downloaded update).
 */
export type CurrentlyRunningInfo = {
  /**
   * The UUID that uniquely identifies the currently running update if `expo-updates` is enabled. The
   * UUID is represented in its canonical string form (`xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx`) and
   * will always use lowercase letters. In development mode, or any other environment in which
   * `expo-updates` is disabled, this value is `null`.
   */
  updateId: string | null;
  /**
   * The channel name of the current build, if configured for use with EAS Update. `null` otherwise.
   */
  channel: string | null;
  /**
   * If `expo-updates` is enabled, this is a `Date` object representing the creation time of the update
   * that's currently running (whether it was embedded or downloaded at runtime).
   *
   * In development mode, or any other environment in which `expo-updates` is disabled, this value is
   * `null`.
   */
  createdAt: Date | null;
  /**
   * This will be true if the currently running update is the one embedded in the build,
   * and not one downloaded from the updates server.
   */
  isEmbeddedLaunch: boolean;
  /**
   * `expo-updates` does its very best to always launch monotonically newer versions of your app so
   * you don't need to worry about backwards compatibility when you put out an update. In very rare
   * cases, it's possible that `expo-updates` may need to fall back to the update that's embedded in
   * the app binary, even after newer updates have been downloaded and run (an "emergency launch").
   * This boolean will be `true` if the app is launching under this fallback mechanism and `false`
   * otherwise. If you are concerned about backwards compatibility of future updates to your app, you
   * can use this constant to provide special behavior for this rare case.
   */
  isEmergencyLaunch: boolean;
  /**
   * If `expo-updates` is enabled, this is the
   * [manifest](/workflow/expo-go#manifest) object for the update that's currently
   * running.
   *
   * In development mode, or any other environment in which `expo-updates` is disabled, this object is
   * empty.
   */
  manifest: Partial<Manifest> | null;
  /**
   * The runtime version of the current build.
   */
  runtimeVersion: string | null;
};

/**
 * Structure representing an available update that has been returned by a call to [`checkForUpdate()`](#checkforupdate)
 * or an [`UpdateEvent`](#updateevent) emitted by native code.
 */
export type AvailableUpdateInfo = {
  /**
   * A string that uniquely identifies the update. For the manifests used in the current Expo Updates protocol (including
   * EAS Update), this represents the update's UUID in its canonical string form (`xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx`)
   * and will always use lowercase letters.
   */
  updateId: string | null;
  /**
   * A `Date` object representing the creation time of the update.
   */
  createdAt: Date | null;
  /**
   * The manifest for the update.
   */
  manifest: Manifest;
};

// Internal type for the callbacks passed into useUpdates()
export type UseUpdatesCallbacksType = {
  onCheckForUpdateStart?: () => void;
  onCheckForUpdateComplete?: () => void;
  onCheckForUpdateError?: (error?: Error) => void;
  onDownloadUpdateStart?: () => void;
  onDownloadUpdateComplete?: () => void;
  onDownloadUpdateError?: (error?: Error) => void;
  onRunUpdateStart?: () => void;
  onRunUpdateError?: (error?: Error) => void;
};

// Internal type used to store the state for the useUpdates() hook

export type UseUpdatesStateType = {
  availableUpdate?: AvailableUpdateInfo;
  error?: Error;
  lastCheckForUpdateTimeSinceRestart?: Date;
  logEntries?: UpdatesLogEntry[];
};
