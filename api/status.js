// Vercel Serverless Function for Gemini Status Check
module.exports = async (req, res) => {
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type, Authorization');

  if (req.method === 'OPTIONS') {
    return res.status(204).end();
  }

  const hasKey = true;
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
};
