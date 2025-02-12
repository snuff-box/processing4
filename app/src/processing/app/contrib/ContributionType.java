/* -*- mode: java; c-basic-offset: 2; indent-tabs-mode: nil -*- */

/*
  Part of the Processing project - http://processing.org

  Copyright (c) 2013-20 The Processing Foundation
  Copyright (c) 2011-12 Ben Fry and Casey Reas

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License version 2
  as published by the Free Software Foundation.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License along
  with this program; if not, write to the Free Software Foundation, Inc.
  59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/
package processing.app.contrib;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import processing.app.Base;
import processing.app.Library;
import processing.app.Messages;
import processing.app.Util;
import processing.app.ui.Editor;


public enum ContributionType {
  LIBRARY, MODE, TOOL, EXAMPLES;


  public String toString() {
    switch (this) {
    case LIBRARY:
      return "library";
    case MODE:
      return "mode";
    case TOOL:
      return "tool";
    case EXAMPLES:
      return "examples";
    default:
      throw new IllegalArgumentException();
    }
  }


  /**
   * Get this type name as a purtied up, capitalized version.
   * @return Mode for mode, Tool for tool, etc.
   */
  public String getTitle() {
    String lower = toString();
    return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
  }


  /*
  public String getPluralTitle() {
    switch (this) {
    case LIBRARY:
      return "Libraries";
    case MODE:
      return "Modes";
    case TOOL:
      return "Tools";
    case EXAMPLES:
      return "Examples";
    }
    return null;  // should be unreachable
  }
  */


//  public String getFolderName() {
//    return toString();
//    /*
//    switch (this) {
//    case LIBRARY:
//      return "libraries";
//    case TOOL:
//      return "tools";
//    case MODE:
//      return "modes";
//    case EXAMPLES:
//      return "examples";
//    }
//    return null;  // should be unreachable
//    */
//  }


  /** Get the name of the properties file for this type of contribution. */
  public String getPropertiesName() {
    return this + ".properties";
  }


  public File createTempFolder() throws IOException {
    return Util.createTempFolder(toString(), "tmp", getSketchbookFolder());
  }


  /*
  // removed for 4.0a6, doesn't appear to be in use
  public File[] listTempFolders() throws IOException {
    File base = getSketchbookFolder();
    return base.listFiles(new FileFilter() {
      @Override
      public boolean accept(File file) {
        String name = file.getName();
        return (file.isDirectory() &&
                name.startsWith(toString()) && name.endsWith("tmp"));
      }
    });
  }
  */


  public boolean isTempFolderName(String name) {
    return name.startsWith(toString()) && name.endsWith("tmp");
  }


//  public String getTempPrefix() {
//    return toString();
//  }
//
//
//  public String getTempSuffix() {
//    return "tmp";
//  }


//    public String getPropertiesName() {
//      return toString() + ".properties";
//    }


  static public ContributionType fromName(String s) {
    if (s != null) {
      if ("library".equalsIgnoreCase(s)) {
        return LIBRARY;
      }
      if ("mode".equalsIgnoreCase(s)) {
        return MODE;
      }
      if ("tool".equalsIgnoreCase(s)) {
        return TOOL;
      }
      if ("examples".equalsIgnoreCase(s)) {
        return EXAMPLES;
      }
    }
    return null;
  }


  public File getSketchbookFolder() {
    switch (this) {
    case LIBRARY:
      return Base.getSketchbookLibrariesFolder();
    case TOOL:
      return Base.getSketchbookToolsFolder();
    case MODE:
      return Base.getSketchbookModesFolder();
    case EXAMPLES:
      return Base.getSketchbookExamplesFolder();
    }
    return null;
  }


  public boolean isCandidate(File potential) {
    return (potential.isDirectory() &&
            new File(potential, toString()).exists() &&
            !isTempFolderName(potential.getName()));
  }


  /**
   * Return a list of directories that have the necessary subfolder for this
   * contribution type. For instance, a list of folders that have a 'mode'
   * subfolder if this is a ModeContribution.
   */
  public File[] listCandidates(File folder) {
    return folder.listFiles(this::isCandidate);
  }


  /**
   * Return the first directory that has the necessary subfolder for this
   * contribution type. For instance, the first folder that has a 'mode'
   * subfolder if this is a ModeContribution.
   */
  File findCandidate(File folder) {
    File[] folders = listCandidates(folder);

    if (folders.length == 0) {
      return null;

    } else if (folders.length > 1) {
      Messages.log("More than one " + this + " found inside " + folder.getAbsolutePath());
    }
    return folders[0];
  }


  /**
   * Returns true if the type of contribution requires the PDE to restart
   * when being added or removed.
   */
  boolean requiresRestart() {
    return this == ContributionType.TOOL || this == ContributionType.MODE;
  }


  LocalContribution load(Base base, File folder) {
    switch (this) {
    case LIBRARY:
      //return new Library(folder);
      return Library.load(folder);
    case TOOL:
      return ToolContribution.load(folder);
    case MODE:
      return ModeContribution.load(base, folder);
    case EXAMPLES:
      return ExamplesContribution.load(folder);
    }
    return null;
  }


  List<LocalContribution> listContributions(Editor editor) {
    List<LocalContribution> contribs = new ArrayList<>();
    switch (this) {
    case LIBRARY:
      contribs.addAll(editor.getMode().contribLibraries);
      break;
    case TOOL:
      contribs.addAll(editor.getBase().getToolContribs());
      break;
    case MODE:
      contribs.addAll(editor.getBase().getModeContribs());
      break;
    case EXAMPLES:
      contribs.addAll(editor.getBase().getExampleContribs());
      break;
    }
    return contribs;
  }


  File getBackupFolder() {
    return new File(getSketchbookFolder(), "old");
  }


  File createBackupFolder(StatusPanel status) {
    File backupFolder = getBackupFolder();
//    if (backupFolder.isDirectory()) {
//      status.setErrorMessage("First remove the folder named \"old\" from the " +
//                             getFolderName() + " folder in the sketchbook.");
//      return null;
//    }
    if (!backupFolder.exists() && !backupFolder.mkdirs()) {
      status.setErrorMessage("Could not create a backup folder in the " +
      		                   "sketchbook " + this + " folder.");
      return null;
    }
    return backupFolder;
  }


//  /**
//   * Create a filter for a specific contribution type.
//   * @param type The type, or null for a generic update checker.
//   */
//  Contribution.Filter createFilter2() {
//    return new Contribution.Filter() {
//      public boolean matches(Contribution contrib) {
//        return contrib.getType() == ContributionType.this;
//      }
//    };
//  }


//  static Contribution.Filter createUpdateFilter() {
//    return new Contribution.Filter() {
//      public boolean matches(Contribution contrib) {
//        if (contrib instanceof LocalContribution) {
//          return ContributionListing.getInstance().hasUpdates(contrib);
//        }
//        return false;
//      }
//    };
//  }
}