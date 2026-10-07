package com.shafagh.application;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
class ArchitectureTest {
 @Test void implementationsArePrivateToTheirModule() {
  var classes=new ClassFileImporter().withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS).importPackages("com.shafagh");
  for(String module: new String[]{"base","cif","dpst","loan","trx"}) {
   noClasses().that().resideOutsideOfPackages("com.shafagh."+module+"..","com.shafagh.application..")
    .should().dependOnClassesThat().resideInAPackage("com.shafagh."+module+".internal..").check(classes);
  }
 }
}
