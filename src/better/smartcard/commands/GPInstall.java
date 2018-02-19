package better.smartcard.commands;

import better.smartcard.gp.GPCard;
import better.smartcard.gp.GPContext;
import better.smartcard.gp.GPIssuerDomain;
import better.smartcard.gp.GPRegistry;
import better.smartcard.gp.protocol.GPPrivilege;
import better.smartcard.util.AID;
import better.smartcard.util.HexUtil;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import javax.smartcardio.CardException;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

@Parameters(
        commandNames = "gp-install",
        commandDescription = "GlobalPlatform: install an applet"
)
public class GPInstall extends GPCommand {

    @Parameter(
            names = "--module",
            description = "Module to install",
            required = true
    )
    AID moduleAID;

    @Parameter(
            names = "--package",
            description = "Package to install",
            required = false
    )
    AID packageAID;

    @Parameter(
            names = "--aid",
            description = "Applet AID to install as",
            required = false
    )
    AID appletAID;

    @Parameter(
            names = "--parameters",
            description = "Pass the given install parameters to the applet",
            required = false
    )
    String appletParameters = "";

    @Parameter(
            names = "--privilege",
            description = "Grant the given privilege to the applet",
            required = false
    )
    List<GPPrivilege> appletPrivileges = new ArrayList<>();

    @Parameter(
            names = "--cap-file",
            description = "CAP files to load",
            required = false
    )
    List<File> capFiles;

    @Parameter(
            names = "--reload",
            description = "Reload provided packages, replacing old versions"
    )
    boolean reload;

    @Parameter(
            names = "--reinstall",
            description = "Reinstall the applet, replacing old instances"
    )
    boolean reinstall;

    public GPInstall(GPContext context) {
        super(context);
    }

    @Override
    protected void performOperation(GPContext context, GPCard card) throws CardException {
        PrintStream os = System.out;
        GPRegistry registry = card.getRegistry();
        GPIssuerDomain issuer = card.getIssuerDomain();

        // reload implies reinstall
        if(reload) {
            reinstall = true;
        }

        // load if requested
        if(capFiles != null && !capFiles.isEmpty()) {
            GPLoad load = new GPLoad(context);
            load.setFiles(capFiles);
            load.setReload(reload);
            load.performOperation(context, card);
            registry.update();
        }

        // determine install parameters
        AID pkgAID = packageAID;
        AID modAID = moduleAID;
        AID appAID = appletAID;

        // default for the app AID
        if(appAID == null) {
            appAID = modAID;
        }

        // default for the pkg AID
        if(pkgAID == null) {
            os.println("Searching card for module " + modAID);
            GPRegistry.ELFEntry elf = registry.findPackageForModule(modAID);
            if(elf == null) {
                throw new Error("Could not find module " + modAID + " on card");
            }
            pkgAID = elf.getAID();
            os.println("Found module in package " + pkgAID);
        }

        // delete previous instance if requested
        if(registry.hasApplet(appAID)) {
            if(reinstall) {
                os.println("Deleting old applet");
                issuer.deleteObject(appAID);
            } else {
                throw new Error("Card already has applet " + appAID);
            }
        }

        // print major parameters
        os.println("Installing applet " + appAID);
        os.println("  package " + pkgAID);
        os.println("  module " + modAID);
        byte[] appPrivs = GPPrivilege.toBytes(appletPrivileges);
        os.println("  privileges " + HexUtil.bytesToHex(appPrivs));
        byte[] appParams = HexUtil.hexToBytes(appletParameters);
        os.println("  parameters " + HexUtil.bytesToHex(appParams));
        os.println();

        // perform the installation
        os.println("Installing now...");
        issuer.installApplet(pkgAID, modAID, appAID, appPrivs, appParams);

        // happy happy joy joy
        os.println("Installation complete");
    }
}
