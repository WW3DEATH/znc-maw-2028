// Vercel Serverless Function for Gemini AI Tutor
const DEFAULT_SYSTEM_INSTRUCTION = `You are the official ZNC Science AI Academic Tutor & Syllabus Mentor for Zahira National College (Mawanella) Science Section.
You specialize in the Sri Lankan G.C.E. Advanced Level (A/L) English Medium and Sinhala/Tamil terminology for:
1. Physics: Mechanics, Thermal Physics, Waves & Oscillations, Optics, Gravitational/Electrostatic/Magnetic Fields, Electricity, Electronics, Radiation & Matter.
2. Combined Mathematics: Pure Mathematics (Algebra, Trigonometry, Calculus, Coordinate Geometry, Complex Numbers) and Applied Mathematics (Statics, Dynamics, Vectors, Probability, Equilibrium, Relative Velocity).
3. Chemistry: Atomic Structure, Chemical Bonding, Calculations (Mole Concept), Gaseous State, Thermodynamics, Chemical Equilibrium, Kinetics, Inorganic Chemistry (s, p, d block), Organic Chemistry, Industrial/Environmental Chemistry.
4. Biology: Cell Biology, Plant Anatomy & Physiology, Animal Anatomy & Physiology, Genetics, Molecular Biology, Environmental Biology, Microbiology.

Tone & Approach:
- Clear, encouraging, highly structured, academic yet easy to understand.
- Provide step-by-step mathematical and scientific derivations with clear explanations for each step.
- Include formulas, units, SI dimensions, and exam tips specific to Sri Lankan A/L past papers.
- Use bullet points, bold key terms, and numbered steps for readability.
- When answering syllabus questions, clearly state the relevant underlying law/principle.`;

const fs = require('fs');
const path = require('path');

// Resolve Gemini API key safely
function getApiKey() {
  if (process.env.GEMINI_API_KEY && process.env.GEMINI_API_KEY !== 'MY_GEMINI_API_KEY') {
    return process.env.GEMINI_API_KEY;
  }
  try {
    if (fs.existsSync('/app/.dev.env.json')) {
      const data = JSON.parse(fs.readFileSync('/app/.dev.env.json', 'utf8'));
      if (data.GEMINI_API_KEY) return data.GEMINI_API_KEY;
    }
  } catch (e) {}
  try {
    const envFile = path.join(__dirname, '../.env');
    if (fs.existsSync(envFile)) {
      const text = fs.readFileSync(envFile, 'utf8');
      const match = text.match(/^\s*GEMINI_API_KEY\s*=\s*(.+)$/m);
      if (match && match[1]) return match[1].trim().replace(/^["']|["']$/g, '');
    }
  } catch (e) {}
  return 'AQ.Ab8RN6KPK2yLtS4YLdbK5Q8hIqOXV1_4EH77nKNXiEH-jQJbjg';
}

async function callGemini(messages, model = 'gemini-3.5-flash', systemInstruction = DEFAULT_SYSTEM_INSTRUCTION) {
  const apiKey = getApiKey();
  if (!apiKey || apiKey === 'MY_GEMINI_API_KEY') {
    throw new Error('GEMINI_API_KEY is not configured in the server environment.');
  }

  const modelsToTry = [
    model,
    'gemini-3.5-flash',
    'gemini-3.5-flash-lite',
    'gemini-3.1-flash-lite',
    'gemini-3.1-pro-preview'
  ];
  const uniqueModels = [...new Set(modelsToTry)];

  let lastError = null;

  for (const candidateModel of uniqueModels) {
    try {
      const formattedContents = [];
      for (const msg of messages) {
        const textContent = (msg && (msg.text || msg.content || msg.prompt)) ? String(msg.text || msg.content || msg.prompt).trim() : '';
        if (!textContent) continue;
        const role = (msg.role === 'model' || msg.role === 'assistant' || msg.role === 'bot') ? 'model' : 'user';
        formattedContents.push({
          role: role,
          parts: [{ text: textContent }]
        });
      }

      if (formattedContents.length === 0) {
        throw new Error('No valid message text provided.');
      }
      if (formattedContents[formattedContents.length - 1].role !== 'user') {
        formattedContents.push({
          role: 'user',
          parts: [{ text: 'Please summarize or continue our discussion.' }]
        });
      }

      const requestBody = {
        contents: formattedContents,
        generationConfig: {
          temperature: 0.7,
          maxOutputTokens: 2048,
          topP: 0.95
        }
      };

      if (systemInstruction) {
        requestBody.systemInstruction = {
          parts: [{ text: systemInstruction }]
        };
      }

      const apiUrl = `https://generativelanguage.googleapis.com/v1beta/models/${candidateModel}:generateContent?key=${apiKey}`;
      const response = await fetch(apiUrl, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(requestBody)
      });

      const data = await response.json();

      if (!response.ok) {
        const errMsg = data.error ? data.error.message : `HTTP ${response.status}`;
        lastError = new Error(errMsg);
        continue;
      }

      const candidate = data.candidates?.[0];
      if (!candidate) {
        throw new Error('No candidate response returned by Gemini.');
      }

      const replyText = candidate.content?.parts?.map(p => p.text || '').join('\n') || '';
      if (!replyText.trim()) {
        throw new Error('Empty text received from Gemini response.');
      }

      return {
        reply: replyText,
        modelUsed: candidateModel,
        finishReason: candidate.finishReason || 'STOP'
      };
    } catch (err) {
      lastError = err;
    }
  }

  throw lastError || new Error('Failed to generate response from Gemini.');
}

module.exports = async (req, res) => {
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET, POST, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type, Authorization');

  if (req.method === 'OPTIONS') {
    return res.status(204).end();
  }

  // Handle status check
  if (req.method === 'GET') {
    const hasKey = !!process.env.GEMINI_API_KEY && process.env.GEMINI_API_KEY !== 'MY_GEMINI_API_KEY';
    return res.status(200).json({
      status: 'ready',
      hasKey: hasKey,
      models: [
        { id: 'gemini-3.5-flash', name: 'Gemini 3.5 Flash (Recommended - General A/L Tasks)', default: true },
        { id: 'gemini-3.1-pro-preview', name: 'Gemini 3.1 Pro (Complex Reasoning & Derivations)' },
        { id: 'gemini-3.1-flash-lite', name: 'Gemini 3.1 Flash-Lite (High Speed & Quick Q&A)' },
        { id: 'gemini-3.8-flash', name: 'Gemini 3.8 Flash' }
      ]
    });
  }

  if (req.method !== 'POST') {
    return res.status(405).json({ error: 'Method Not Allowed. Use POST.' });
  }

  try {
    const payload = typeof req.body === 'string' ? JSON.parse(req.body || '{}') : (req.body || {});
    const { messages, model, systemInstruction } = payload;

    if (!messages || !Array.isArray(messages) || messages.length === 0) {
      return res.status(400).json({ error: 'Invalid request: "messages" array is required.' });
    }

    const result = await callGemini(messages, model, systemInstruction);
    return res.status(200).json({
      success: true,
      reply: result.reply,
      modelUsed: result.modelUsed,
      finishReason: result.finishReason
    });
  } catch (err) {
    console.error('Vercel Gemini API Error:', err);
    return res.status(500).json({
      success: false,
      error: err.message || 'Error processing Gemini request.'
    });
  }
};
