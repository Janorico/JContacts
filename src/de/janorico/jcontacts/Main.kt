/*
 * JContacts is a simple contact software, written in Kotlin.
 * Copyright (C) 2023 Janosch Lion
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package de.janorico.jcontacts

import de.janorico.jcontacts.data.UDM
import de.janorico.jcontacts.gui.*
import de.janorico.jgl.JGL
import de.janorico.jgl.helpers.SplashScreen
import java.awt.Container
import javax.swing.JCheckBox

val logos = listOf(
    RM.getImage("JContactsLogo16"),
    RM.getImage("JContactsLogo20"),
    RM.getImage("JContactsLogo24"),
    RM.getImage("JContactsLogo32"),
    RM.getImage("JContactsLogo40"),
    RM.getImage("JContactsLogo48"),
    RM.getImage("JContactsLogo64"),
    RM.getImage("JContactsLogo96")
)

fun main() {
    println(Dialogs.copyright)
    JGL.programName = "JContacts"
    UDM.refresh()
    SplashScreen.show(RM.getIcon("JContactsLogo1024"), 2000)
    MainFrame()
}

fun <T> T?.notNull(onNotNull: T.() -> Unit) {
    this?.onNotNull()
}

fun <T> T?.ifNull(checkBox: JCheckBox, c: Container, notNull: T.() -> Unit) {
    if (this == null) {
        checkBox.isSelected = false
        c.isEnabled = false
    } else {
        checkBox.isSelected = true
        c.isEnabled = true
        this.notNull()
    }
}

fun <This, Other, Out> This?.notNull(other: Other?, defaultValue: Out, onNotNull: This.(other: Other) -> Out): Out =
    if (this == null || other == null) defaultValue else this.onNotNull(other)

fun String.intOrNull(): Int? = if (isBlank()) null
else toInt()

fun String?.notNull(): String = if (isNullOrBlank()) "none" else this

object Jobs {
    // Source: https://visualdictionary.org/list-of-jobs/, 26.2.2023 (Sorted alphabetical)
    val jobs = arrayOf(
        "",
        "Accountant",
        "Actor",
        "Actress",
        "Advisor",
        "Ambassador",
        "Animator",
        "Archer",
        "Architect",
        "Artist",
        "Astronaut",
        "Astronomer",
        "Athlete",
        "Attorney",
        "Auctioneer",
        "Author",
        "Babysitter",
        "Baker",
        "Ballet dancer",
        "Banker",
        "Barber",
        "Bartender",
        "Baseball player",
        "Basketball player",
        "Biologist",
        "Bookkeeper",
        "Bowler",
        "Boxer",
        "Builder",
        "Bus driver",
        "Butcher",
        "Butler",
        "Cab driver",
        "Calligrapher",
        "Captain",
        "Cardiologist",
        "Caregiver",
        "Carpenter",
        "Cartographer",
        "Cartoonist",
        "Cashier",
        "Catcher",
        "Caterer",
        "Cellist",
        "Chauffeur",
        "Chef",
        "Chemist",
        "Clergyman",
        "Clergywoman",
        "Clerk",
        "Coach",
        "Cobbler",
        "Composer",
        "Concierge",
        "Coroner",
        "Counselor",
        "Courier",
        "Cryptographer",
        "Custodian",
        "Dancer",
        "Dentist",
        "Deputy",
        "Dermatologist",
        "Designer",
        "Detective",
        "Director",
        "Diver",
        "Doctor",
        "Doorman",
        "Driver",
        "Drummer",
        "Ecologist",
        "Economist",
        "Editor",
        "Educator",
        "Electrician",
        "Engineer",
        "Entertainer",
        "Entomologist",
        "Entrepreneur",
        "Executive",
        "Explorer",
        "Exporter",
        "Exterminator",
        "Falconer",
        "Farmer",
        "Filmmaker",
        "Financier",
        "Firefighter",
        "Fisherman",
        "Florist",
        "Football player",
        "Garbage man",
        "Gardener",
        "Gatherer",
        "Gemcutter",
        "Geneticist",
        "Geographer",
        "Golfer",
        "Governor",
        "Grocer",
        "Hairdresser",
        "Harpist",
        "Housewife",
        "Hunter",
        "Illustrator",
        "Importer",
        "Instructor",
        "Intern",
        "Internist",
        "Interpreter",
        "Inventor",
        "Investigator",
        "Jailer",
        "Janitor",
        "Jester",
        "Jeweler",
        "Jockey",
        "Journalist",
        "Judge",
        "Laborer",
        "Landlord",
        "Laundress",
        "Lawyer",
        "Lecturer",
        "Librarian",
        "Librettist",
        "Lifeguard",
        "Linguist",
        "Locksmith",
        "Lyricist",
        "Magician",
        "Maid",
        "Mail carrier",
        "Manager",
        "Manga artist",
        "Manufacturer",
        "Marketer",
        "Mathematician",
        "Mayor",
        "Mechanic",
        "Midwife",
        "Miner",
        "Model",
        "Musician",
        "Navigator",
        "Negotiator",
        "Notary",
        "Novelist",
        "Nun",
        "Nurse",
        "Nutritionist",
        "Office worker",
        "Operator",
        "Ophthalmologist",
        "Optician",
        "Ornithologist",
        "Painter",
        "Paleontologist",
        "Paramedic",
        "Park ranger",
        "Pathologist",
        "Pawnbroker",
        "Pediatrician",
        "Percussionist",
        "Performer",
        "Pharmacist",
        "Photographer",
        "Physician",
        "Pianist",
        "Pilot",
        "Pitcher",
        "Plumber",
        "Poet",
        "Police",
        "Politician",
        "Pope",
        "Postman",
        "President",
        "Priest",
        "Producer",
        "Professor",
        "Programmer",
        "Psychologist",
        "Publisher",
        "Quarterback",
        "Radiologist",
        "Rancher",
        "Ranger",
        "Real estate agent",
        "Receptionist",
        "Referee",
        "Registrar",
        "Reporter",
        "Representative",
        "Researcher",
        "Restauranteur",
        "Retailer",
        "Retiree",
        "Sailor",
        "Salesperson",
        "Saleswoman",
        "Samurai",
        "Saxophonist",
        "Scholar",
        "Scientist",
        "Scout",
        "Scuba diver",
        "Seamstress",
        "Secretary",
        "Security guard",
        "Senator",
        "Sheriff",
        "Singer",
        "Smith",
        "Soldier",
        "Spy",
        "Statistician",
        "Stockbroker",
        "Street sweeper",
        "Student",
        "Surgeon",
        "Surveyor",
        "Swimmer",
        "Tailor",
        "Tax collector",
        "Taxi driver",
        "Taxidermist",
        "Teacher",
        "Technician",
        "Tennis player",
        "Therapist",
        "Tour guide",
        "Trader",
        "Trainer",
        "Translator",
        "Trash collector",
        "Treasurer",
        "Truck driver",
        "Tutor",
        "Typist",
        "Umpire",
        "Undertaker",
        "Valet",
        "Veteran",
        "Veterinarian",
        "Vicar",
        "Videographer",
        "Violinist",
        "Waiter",
        "Waitress",
        "Warden",
        "Warrior",
        "Watchmaker",
        "Weaver",
        "Welder",
        "Woodcarver",
        "Workman",
        "Writer",
        "Zookeeper",
        "Zoologist"
    )
}
