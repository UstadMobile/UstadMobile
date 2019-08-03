const path = require('path');
const fs = require('fs-extra');
let cp = require("child_process");
const moduleList = createModuleList();
const packages = [];
let tmpPackages = [];

//Create list of all packages to be installed
function createModuleList(){
    let modules = []
    const moduleDir = path.resolve('../build/js/node_modules/')
    fs.readdirSync(moduleDir).forEach(file => {
        if(fs.existsSync(path.resolve(moduleDir+'/'+file+'/package.json'))  && !file.includes("UstadMobile-core")
        && !file.includes("UstadMobile-lib-util") && file.indexOf("-test") == -1){
            modules.push(path.resolve(moduleDir+'/'+file))
        }
      });
      modules.splice(0,0,path.resolve(moduleDir+'/UstadMobile-lib-util'))
      modules.splice(0,0,path.resolve(moduleDir+'/UstadMobile-core'));
      return modules;
}


//Create NPM package from the module 
function createPackage(modulePath){
    cp.exec("sudo npm pack", {cwd: modulePath}, function(error,stdout,stderr){
        const package = stdout.replace('\n','')
        console.log(error == null ?  "Packaged ====> "+package : stderr)
        packages.push(path.resolve(modulePath+'/'+package))
        if(moduleList.length != 0){
            createPackage(moduleList.pop())
        }else{
            tmpPackages = tmpPackages.concat(packages)
            console.log("All "+packages.length+" modules were packaged successfully")
            console.log("Installing.......")
            installPackage(packages.shift())
        }
    });
}

//Install package 
function installPackage(packagePath){
    cp.exec("sudo npm install "+packagePath, {cwd: path.resolve("./")}, function(error,stdout,stderr){
        console.log("Installed ====> "+(packagePath.split("/")[packagePath.split("/").length -1]))
        if(packages.length != 0){
            installPackage(packages.shift())
        }else{
            console.log("All packages were installed successfully")
            console.log("Removing temp package file......")
            //deletePackageFile(tmpPackages.shift())
        }
    });
}

//Delete temp package file
function deletePackageFile(packagePath) {
    cp.exec("sudo chmod 777 "+packagePath+"; sudo rm " + packagePath, {
        cwd: packagePath.replace(packagePath.split("/")[packagePath.split("/").length - 1],"")
    }, function (error, stdout, stderr) {
        console.log(error == null ? "Removed tmp ====> " + (packagePath.split("/")[packagePath.split("/").length - 1])
        : "Failed to remove a file")
        if (tmpPackages.length != 0) {
            deletePackageFile(tmpPackages.shift())
        } else {
            console.log("All temp package files were deleted successfully")
        }
    });
}
console.log("Packaging .........");
createPackage(moduleList.pop())
