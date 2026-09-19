package com.example.data

import com.example.model.AcademicStream
import com.example.model.QuizQuestion
import com.example.model.SyllabusSubject
import com.example.model.SyllabusUnit

object SyllabusData {

    val combinedMathsUnits = listOf(
        SyllabusUnit(1, "Real Number System & Polynomials", "Real numbers, modulus inequalities, remainder theorem, partial fractions"),
        SyllabusUnit(2, "Quadratic Functions & Equations", "Roots of quadratic equations, discriminant, sign of quadratic expressions"),
        SyllabusUnit(3, "Matrices and Determinants", "Matrix operations, inverse matrix, determinants and systems of linear equations"),
        SyllabusUnit(4, "Trigonometric Functions & Identities", "Compound angles, multiple angles, general solutions, sine & cosine rules"),
        SyllabusUnit(5, "Differentiation & Applications", "Product, quotient, chain rule, tangents, normals, maxima and minima"),
        SyllabusUnit(6, "Integration & Definite Integrals", "Standard integrals, substitution, by parts, partial fractions, areas"),
        SyllabusUnit(7, "Statics of Rigid Bodies", "Equilibrium under coplanar forces, moments, couples, friction and centers of gravity"),
        SyllabusUnit(8, "Dynamics & Newton's Laws", "Rectilinear motion, projectiles, Newton's laws, impulse and momentum, work, energy & power")
    )

    val physicsUnits = listOf(
        SyllabusUnit(1, "Units and Measurement", "SI units, dimensions, errors, vernier calipers, micrometer screw gauge"),
        SyllabusUnit(2, "Mechanics", "Vectors, rectilinear motion, circular motion, gravitational force, moments of inertia"),
        SyllabusUnit(3, "Oscillations and Waves", "Simple harmonic motion, wave properties, Doppler effect, resonance, wave optics"),
        SyllabusUnit(4, "Thermal Physics", "Temperature scales, calorimetry, ideal gas laws, thermal expansion, conduction & radiation"),
        SyllabusUnit(5, "Gravitational & Electrostatic Fields", "Newton's gravitation, Coulomb's law, electric field intensity, potential, Gauss's law, capacitors"),
        SyllabusUnit(6, "Current Electricity & Magnetic Fields", "Ohm's law, Kirchhoff's laws, potentiometer, Biot-Savart, Lorentz force, Faraday's induction"),
        SyllabusUnit(7, "Electronics & Operational Amplifiers", "Semiconductors, diodes, rectification, bipolar transistors, op-amps in inverting/non-inverting"),
        SyllabusUnit(8, "Radiation, Matter & Modern Physics", "Photoelectric effect, de Broglie wavelength, Bohr model, radioactivity, nuclear energy")
    )

    val chemistryUnits = listOf(
        SyllabusUnit(1, "Atomic Structure & Periodic Properties", "Quantum numbers, electronic configurations, orbital shapes, periodic trends"),
        SyllabusUnit(2, "Structure & Chemical Bonding", "Ionic, covalent, metallic bonding, VSEPR theory, hybridization, intermolecular forces"),
        SyllabusUnit(3, "Chemical Calculations & Mole Concept", "Empirical & molecular formulas, stoichiometry, molarity, gas volume calculations, redox titrations"),
        SyllabusUnit(4, "Gaseous State", "Ideal gas equation, Dalton's law of partial pressures, Graham's law, van der Waals equation"),
        SyllabusUnit(5, "Energetics & Chemical Thermodynamics", "Hess's law, standard enthalpies of formation, lattice energy, Born-Haber cycle, Gibbs free energy"),
        SyllabusUnit(6, "Inorganic Chemistry (s, p & d Block)", "Reactivity of Group 1, 2, 13-17 elements, transition metals, color, complex ions"),
        SyllabusUnit(7, "Basic Principles of Organic Chemistry", "IUPAC nomenclature, isomerism (structural & stereo), reaction intermediates, inductive/mesomeric effects"),
        SyllabusUnit(8, "Hydrocarbons & Alkyl Halides", "Alkanes, alkenes, alkynes, benzene electrophilic substitution, nucleophilic substitution SN1/SN2"),
        SyllabusUnit(9, "Chemical Kinetics", "Rate laws, order of reaction, activation energy, Arrhenius equation, catalysis"),
        SyllabusUnit(10, "Chemical & Ionic Equilibrium", "Equilibrium constant Kc/Kp, Le Chatelier's principle, pH, buffers, solubility product Ksp")
    )

    val biologyUnits = listOf(
        SyllabusUnit(1, "Introduction to Biology & Chemical Basis", "Characteristics of living organisms, water properties, carbohydrates, lipids, proteins, nucleic acids"),
        SyllabusUnit(2, "Cellular Basis of Life", "Microscopy, organelle ultra-structure, cell cycle, mitosis, meiosis, enzyme kinetics"),
        SyllabusUnit(3, "Evolution & Diversity of Organisms", "Origin of life, Darwinian evolution, 3-domain classification, plant & animal diversity"),
        SyllabusUnit(4, "Plant Form & Function", "Plant anatomy, water potential & transpiration, mineral nutrition, C3/C4 photosynthesis, plant hormones"),
        SyllabusUnit(5, "Animal Form & Function", "Digestive, circulatory, respiratory, excretory, nervous, endocrine and immune systems"),
        SyllabusUnit(6, "Genetics & Heredity Patterns", "Mendelian genetics, monohybrid/dihybrid crosses, sex linkage, gene interactions, pedigrees"),
        SyllabusUnit(7, "Molecular Biology & Recombinant DNA", "DNA replication, transcription, genetic code, translation, PCR, gel electrophoresis, GMOs"),
        SyllabusUnit(8, "Environmental Biology & Ecology", "Ecosystem dynamics, energy flow, biogeochemical cycles, biomes of Sri Lanka, conservation"),
        SyllabusUnit(9, "Applied Biology", "Food technology, industrial microbiology, disease management, aquaculture, bio-fertilizers")
    )

    val ictUnits = listOf(
        SyllabusUnit(1, "Basic Concepts of ICT", "Data vs information, computer generation, classification, social and ethical aspects"),
        SyllabusUnit(2, "Architecture of Computers", "Von Neumann architecture, CPU registers, ALU, CU, memory hierarchy, cache"),
        SyllabusUnit(3, "Data Representation & Logic Gates", "Binary, octal, hex conversions, two's complement, BCD, ASCII, Unicode, Boolean algebra, logic circuits"),
        SyllabusUnit(4, "Fundamentals of Computer Networks", "Network topologies, OSI & TCP/IP models, IP addressing IPv4/IPv6, subnetting, network devices"),
        SyllabusUnit(5, "Operating Systems", "Process scheduling, memory management, file systems, device management, command line"),
        SyllabusUnit(6, "Database Management Systems", "Relational database concepts, ER diagrams, normalization (1NF, 2NF, 3NF), SQL queries"),
        SyllabusUnit(7, "Programming Concepts & Python", "Control structures, data structures (lists, tuples, dicts), modular programming, algorithms"),
        SyllabusUnit(8, "Web Development & Internet Technologies", "HTML5, CSS3, JavaScript basics, client-server web architecture, HTTP/HTTPS")
    )

    val allSubjects = listOf(
        SyllabusSubject(
            id = "cm",
            name = "Combined Mathematics",
            code = "MATH12",
            streams = listOf(AcademicStream.PHYSICAL_SCIENCE),
            units = combinedMathsUnits
        ),
        SyllabusSubject(
            id = "phy",
            name = "Physics",
            code = "PHYS12",
            streams = listOf(AcademicStream.PHYSICAL_SCIENCE, AcademicStream.BIO_SCIENCE),
            units = physicsUnits
        ),
        SyllabusSubject(
            id = "chem",
            name = "Chemistry",
            code = "CHEM12",
            streams = listOf(AcademicStream.PHYSICAL_SCIENCE, AcademicStream.BIO_SCIENCE),
            units = chemistryUnits
        ),
        SyllabusSubject(
            id = "bio",
            name = "Biology",
            code = "BIOL12",
            streams = listOf(AcademicStream.BIO_SCIENCE),
            units = biologyUnits
        ),
        SyllabusSubject(
            id = "ict",
            name = "ICT",
            code = "ICT12",
            streams = listOf(AcademicStream.PHYSICAL_SCIENCE, AcademicStream.BIO_SCIENCE),
            units = ictUnits
        )
    )

    fun getSubjectsForStream(stream: AcademicStream): List<SyllabusSubject> {
        return when (stream) {
            AcademicStream.PHYSICAL_SCIENCE -> allSubjects.filter { it.streams.contains(AcademicStream.PHYSICAL_SCIENCE) }
            AcademicStream.BIO_SCIENCE -> allSubjects.filter { it.streams.contains(AcademicStream.BIO_SCIENCE) }
            AcademicStream.GENERAL -> allSubjects
        }
    }

    // Default Bank of Sri Lankan A/L English Medium Questions
    val questionBank: List<QuizQuestion> = listOf(
        // Combined Maths Unit 1
        QuizQuestion(
            id = "cm_1_1",
            subjectId = "cm",
            subjectName = "Combined Mathematics",
            unitNumber = 1,
            unitName = "Real Number System & Polynomials",
            questionText = "If the polynomial P(x) = x^3 + ax^2 - 5x + 6 is divisible by (x - 2), what is the value of constant 'a'?",
            options = listOf("-1", "1", "-2", "2"),
            correctIndex = 0,
            explanation = "By Remainder Theorem, P(2) = 0. 2^3 + a(2)^2 - 5(2) + 6 = 0 => 8 + 4a - 10 + 6 = 0 => 4a + 4 = 0 => a = -1."
        ),
        QuizQuestion(
            id = "cm_2_1",
            subjectId = "cm",
            subjectName = "Combined Mathematics",
            unitNumber = 2,
            unitName = "Quadratic Functions & Equations",
            questionText = "If α and β are the roots of the equation 2x^2 - 4x + 1 = 0, what is the value of (α^2 + β^2)?",
            options = listOf("3", "2", "4", "7/2"),
            correctIndex = 0,
            explanation = "α + β = 4/2 = 2 and αβ = 1/2. α^2 + β^2 = (α + β)^2 - 2αβ = 2^2 - 2(1/2) = 4 - 1 = 3."
        ),
        QuizQuestion(
            id = "cm_5_1",
            subjectId = "cm",
            subjectName = "Combined Mathematics",
            unitNumber = 5,
            unitName = "Differentiation & Applications",
            questionText = "What is the derivative of y = ln(sec x + tan x) with respect to x?",
            options = listOf("sec x", "tan x", "sec x * tan x", "cos x"),
            correctIndex = 0,
            explanation = "dy/dx = (1/(sec x + tan x)) * (sec x tan x + sec^2 x) = sec x(tan x + sec x) / (sec x + tan x) = sec x."
        ),
        QuizQuestion(
            id = "cm_8_1",
            subjectId = "cm",
            subjectName = "Combined Mathematics",
            unitNumber = 8,
            unitName = "Dynamics & Newton's Laws",
            questionText = "A projectile is launched with velocity u at an angle θ to the horizontal. What is its maximum height H reached above the horizontal plane?",
            options = listOf("u^2 sin^2 θ / (2g)", "u^2 sin 2θ / g", "u^2 cos^2 θ / (2g)", "u sin θ / g"),
            correctIndex = 0,
            explanation = "Using v_y^2 = u_y^2 - 2gH with v_y = 0 at top: 0 = (u sin θ)^2 - 2gH => H = u^2 sin^2 θ / (2g)."
        ),

        // Physics
        QuizQuestion(
            id = "phy_1_1",
            subjectId = "phy",
            subjectName = "Physics",
            unitNumber = 1,
            unitName = "Units and Measurement",
            questionText = "Which of the following physical quantities has the SI base unit kg m^-1 s^-2?",
            options = listOf("Pressure (or Stress)", "Energy", "Power", "Momentum"),
            correctIndex = 0,
            explanation = "Pressure = Force / Area = (kg m s^-2) / m^2 = kg m^-1 s^-2 (Pascals)."
        ),
        QuizQuestion(
            id = "phy_2_1",
            subjectId = "phy",
            subjectName = "Physics",
            unitNumber = 2,
            unitName = "Mechanics",
            questionText = "A car of mass 1000 kg rounds a flat unbanked curve of radius 50 m at 20 m/s. What minimum coefficient of static friction is required?",
            options = listOf("0.8", "0.4", "0.6", "0.2"),
            correctIndex = 0,
            explanation = "f_s = μ m g = m v^2 / r => μ = v^2 / (r g) = (20)^2 / (50 * 10) = 400 / 500 = 0.8."
        ),
        QuizQuestion(
            id = "phy_3_1",
            subjectId = "phy",
            subjectName = "Physics",
            unitNumber = 3,
            unitName = "Oscillations and Waves",
            questionText = "A sound source moves towards a stationary observer with speed v_s. If the speed of sound is v, the apparent frequency observed f' is:",
            options = listOf("f * (v / (v - v_s))", "f * (v / (v + v_s))", "f * ((v + v_s) / v)", "f * ((v - v_s) / v)"),
            correctIndex = 0,
            explanation = "When a source moves towards a stationary listener, wavefronts compress: f' = f * (v / (v - v_s))."
        ),
        QuizQuestion(
            id = "phy_6_1",
            subjectId = "phy",
            subjectName = "Physics",
            unitNumber = 6,
            unitName = "Current Electricity & Magnetic Fields",
            questionText = "A charged particle q with velocity v enters perpendicularly into a uniform magnetic field B. What is the radius of the circular path?",
            options = listOf("m v / (q B)", "q B / (m v)", "q v / (m B)", "m B / (q v)"),
            correctIndex = 0,
            explanation = "Centripetal force is provided by magnetic Lorentz force: m v^2 / r = q v B => r = m v / (q B)."
        ),

        // Chemistry
        QuizQuestion(
            id = "chem_1_1",
            subjectId = "chem",
            subjectName = "Chemistry",
            unitNumber = 1,
            unitName = "Atomic Structure & Periodic Properties",
            questionText = "What are the four quantum numbers for the 19th electron (valence electron) of Potassium (K, Z=19)?",
            options = listOf("n=4, l=0, m_l=0, m_s=+1/2", "n=3, l=2, m_l=0, m_s=+1/2", "n=4, l=1, m_l=0, m_s=-1/2", "n=3, l=0, m_l=0, m_s=+1/2"),
            correctIndex = 0,
            explanation = "Potassium configuration is 1s^2 2s^2 2p^6 3s^2 3p^6 4s^1. For 4s: n=4, l=0 (s orbital), m_l=0, m_s=+1/2."
        ),
        QuizQuestion(
            id = "chem_3_1",
            subjectId = "chem",
            subjectName = "Chemistry",
            unitNumber = 3,
            unitName = "Chemical Calculations & Mole Concept",
            questionText = "How many moles of KMnO4 are required to oxidize 5 moles of Fe^2+ in acidic aqueous solution?",
            options = listOf("1 mole", "5 moles", "2.5 moles", "0.2 mole"),
            correctIndex = 0,
            explanation = "The redox half reactions are: MnO4^- + 8H^+ + 5e^- -> Mn^2+ + 4H2O and Fe^2+ -> Fe^3+ + e^-. Ratio is 1 MnO4^- : 5 Fe^2+."
        ),
        QuizQuestion(
            id = "chem_7_1",
            subjectId = "chem",
            subjectName = "Chemistry",
            unitNumber = 7,
            unitName = "Basic Principles of Organic Chemistry",
            questionText = "Which carbocation intermediate is the most stable among the following?",
            options = listOf("(CH3)3C^+ (tert-butyl)", "(CH3)2CH^+ (isopropyl)", "CH3CH2^+ (ethyl)", "CH3^+ (methyl)"),
            correctIndex = 0,
            explanation = "Tertiary carbocation is stabilized by 9 hyperconjugative alpha-hydrogens and positive inductive (+I) effect of three methyl groups."
        ),
        QuizQuestion(
            id = "chem_10_1",
            subjectId = "chem",
            subjectName = "Chemistry",
            unitNumber = 10,
            unitName = "Chemical & Ionic Equilibrium",
            questionText = "According to Le Chatelier's principle, an exothermic gaseous reaction N2(g) + 3H2(g) <=> 2NH3(g) (ΔH < 0) will shift forward by:",
            options = listOf("Decreasing temperature and increasing pressure", "Increasing temperature and decreasing pressure", "Adding catalyst only", "Decreasing volume and increasing temperature"),
            correctIndex = 0,
            explanation = "Forward reaction is exothermic (favored by lower temp) and decreases moles of gas (4 mol -> 2 mol, favored by high pressure)."
        ),

        // Biology
        QuizQuestion(
            id = "bio_1_1",
            subjectId = "bio",
            subjectName = "Biology",
            unitNumber = 1,
            unitName = "Introduction to Biology & Chemical Basis",
            questionText = "Which bond is responsible for stabilizing the alpha-helix secondary structure of a protein?",
            options = listOf("Hydrogen bonds between C=O and N-H of peptide backbone", "Disulfide covalent bonds between cysteines", "Ionic bonds between R groups", "Hydrophobic interactions"),
            correctIndex = 0,
            explanation = "Alpha-helix is stabilized by hydrogen bonding between the C=O of one amino acid and the N-H of an amino acid four residues ahead."
        ),
        QuizQuestion(
            id = "bio_4_1",
            subjectId = "bio",
            subjectName = "Biology",
            unitNumber = 4,
            unitName = "Plant Form & Function",
            questionText = "During light reactions of photosynthesis, photolysis of water directly provides electrons to which complex?",
            options = listOf("Photosystem II (P680)", "Photosystem I (P700)", "Cytochrome b6f complex", "ATP synthase"),
            correctIndex = 0,
            explanation = "Water splitting enzyme associated with PS II (P680 reaction center) replaces the electrons excited by photons."
        ),
        QuizQuestion(
            id = "bio_6_1",
            subjectId = "bio",
            subjectName = "Biology",
            unitNumber = 6,
            unitName = "Genetics & Heredity Patterns",
            questionText = "In a cross between AaBb x aabb (test cross), assuming independent assortment, what is the expected phenotypic ratio?",
            options = listOf("1 : 1 : 1 : 1", "9 : 3 : 3 : 1", "3 : 1", "1 : 2 : 1"),
            correctIndex = 0,
            explanation = "A dihybrid test cross with non-linked genes produces 4 equal classes: AB, Ab, aB, ab in a 1:1:1:1 ratio."
        ),
        QuizQuestion(
            id = "bio_7_1",
            subjectId = "bio",
            subjectName = "Biology",
            unitNumber = 7,
            unitName = "Molecular Biology & Recombinant DNA",
            questionText = "Which thermostable enzyme is utilized during the extension step of Polymerase Chain Reaction (PCR)?",
            options = listOf("Taq DNA Polymerase", "DNA Ligase", "Reverse Transcriptase", "Restriction Endonuclease EcoRI"),
            correctIndex = 0,
            explanation = "Taq polymerase isolated from Thermus aquaticus survives high denaturation temperatures (95°C) and synthesizes DNA at 72°C."
        ),

        // ICT
        QuizQuestion(
            id = "ict_3_1",
            subjectId = "ict",
            subjectName = "ICT",
            unitNumber = 3,
            unitName = "Data Representation & Logic Gates",
            questionText = "What is the 8-bit Two's Complement representation of decimal integer -42?",
            options = listOf("11010110", "10101010", "11101010", "00101010"),
            correctIndex = 0,
            explanation = "+42 in 8-bit binary is 00101010. One's complement is 11010101. Add 1 to get Two's complement: 11010110."
        ),
        QuizQuestion(
            id = "ict_4_1",
            subjectId = "ict",
            subjectName = "ICT",
            unitNumber = 4,
            unitName = "Fundamentals of Computer Networks",
            questionText = "At which layer of the OSI model does the Internet Protocol (IP) operate?",
            options = listOf("Network Layer (Layer 3)", "Data Link Layer (Layer 2)", "Transport Layer (Layer 4)", "Application Layer (Layer 7)"),
            correctIndex = 0,
            explanation = "IP is a connectionless network layer protocol responsible for logical addressing and packet routing."
        ),
        QuizQuestion(
            id = "ict_6_1",
            subjectId = "ict",
            subjectName = "ICT",
            unitNumber = 6,
            unitName = "Database Management Systems",
            questionText = "Which SQL keyword is used to eliminate duplicate rows from a SELECT query result?",
            options = listOf("DISTINCT", "UNIQUE", "GROUP BY", "DIFFERENT"),
            correctIndex = 0,
            explanation = "The SELECT DISTINCT statement is used to return only distinct (different) values."
        ),
        QuizQuestion(
            id = "ict_7_1",
            subjectId = "ict",
            subjectName = "ICT",
            unitNumber = 7,
            unitName = "Programming Concepts & Python",
            questionText = "In Python, what is the output of len(set([1, 2, 2, 3, 4, 4, 4, 5]))?",
            options = listOf("5", "8", "4", "Error"),
            correctIndex = 0,
            explanation = "The set function removes all duplicates, leaving {1, 2, 3, 4, 5}, which has a length of 5."
        )
    )

    fun getQuestionsForUnit(subjectId: String, unitNumber: Int): List<QuizQuestion> {
        val matches = questionBank.filter { it.subjectId == subjectId && it.unitNumber == unitNumber }
        if (matches.isNotEmpty()) return matches
        // Fallback or generic questions for this unit
        val sub = allSubjects.firstOrNull { it.id == subjectId }
        val unit = sub?.units?.firstOrNull { it.unitNumber == unitNumber }
        val unitName = unit?.unitName ?: "Unit $unitNumber"
        val subName = sub?.name ?: "Subject"

        return listOf(
            QuizQuestion(
                id = "${subjectId}_${unitNumber}_q1",
                subjectId = subjectId,
                subjectName = subName,
                unitNumber = unitNumber,
                unitName = unitName,
                questionText = "In Sri Lankan A/L $subName ($unitName), what is the key fundamental principle underlying standard examination problems?",
                options = listOf(
                    "Conservation laws and rigorous theoretical definitions",
                    "Random estimation without empirical validation",
                    "Purely qualitative memory recall without units",
                    "Ignoring standard SI units and boundary limits"
                ),
                correctIndex = 0,
                explanation = "In the G.C.E. Advanced Level English medium syllabus, $unitName questions strictly test conceptual derivation, mathematical accuracy, and conservation laws."
            ),
            QuizQuestion(
                id = "${subjectId}_${unitNumber}_q2",
                subjectId = subjectId,
                subjectName = subName,
                unitNumber = unitNumber,
                unitName = unitName,
                questionText = "When evaluating experimental data for $unitName, which factor is crucial to minimize systematic errors?",
                options = listOf(
                    "Proper calibration of instruments and zero-error corrections",
                    "Taking only a single measurement",
                    "Discarding fractional significant figures arbitrarily",
                    "Increasing parallax displacement intentionally"
                ),
                correctIndex = 0,
                explanation = "Systematic errors are eliminated through accurate zero correction, calibration against standards, and precise environmental control."
            )
        )
    }
}
