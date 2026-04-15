import pfp from '../../assets/images/example.jpg';

export default function Message({ name, onClick }) {
  return (
    <div className="message" onClick={onClick} style={{ cursor: 'pointer', padding: '10px', borderBottom: '1px solid #eee' }}>
      <div id="pfp-container">
        <img id="pfp" src={pfp} alt="profile" style={{ width: '40px', borderRadius: '50%' }} />
      </div>
      <div id="msg-container">
        <b id="user">{name}</b>
      </div>
    </div>
  );
}